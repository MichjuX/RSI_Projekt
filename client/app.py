from flask import Flask, render_template, request, redirect, url_for, send_file, session, flash
from zeep import Client
from zeep.transports import Transport
import requests
import io
import urllib3
import os
from functools import wraps

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', 'secret_key')

WSDL_URL = os.getenv('EVENTS_WSDL_URL', 'https://localhost:8443/ws/events?wsdl')
WEATHER_WSDL_URL = os.getenv('WEATHER_WSDL_URL', 'http://localhost:8444/ws/weather?wsdl')

ADMIN_USER = 'admin'
ADMIN_PASS = 'admin123'


def get_soap_client():
    sess = requests.Session()
    sess.verify = False
    transport = Transport(session=sess)
    return Client(wsdl=WSDL_URL, transport=transport)


def admin_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not session.get('is_admin'):
            flash('Wymagane zalogowanie jako administrator.')
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated


@app.route('/login', methods=['GET', 'POST'])
def login():
    if session.get('is_admin'):
        return redirect(url_for('index'))
    error = None
    if request.method == 'POST':
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '')
        if username == ADMIN_USER and password == ADMIN_PASS:
            session['is_admin'] = True
            session['username'] = username
            return redirect(url_for('index'))
        error = 'Nieprawidłowy login lub hasło.'
    return render_template('login.html', error=error)


@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('index'))


@app.route('/')
def index():
    try:
        client = get_soap_client()
        events = client.service.getAllEvents()
        return render_template('index.html', events=events, error=None)
    except Exception as e:
        return render_template('index.html', events=[], error=str(e))


@app.route('/upcoming')
def upcoming():
    try:
        client = get_soap_client()
        events = client.service.getUpcomingEvents()
        return render_template('index.html', events=events, error=None)
    except Exception as e:
        return render_template('index.html', events=[], error=str(e))


@app.route('/search_day', methods=['POST'])
def search_day():
    date = request.form.get('date')
    try:
        client = get_soap_client()
        events = client.service.getEventsByDay(date=date)
        return render_template('index.html', events=events)
    except Exception as e:
        return render_template('index.html', events=[], error=str(e))


@app.route('/search_week', methods=['POST'])
def search_week():
    week = int(request.form.get('week'))
    year = int(request.form.get('year'))
    try:
        client = get_soap_client()
        events = client.service.getEventsByWeek(week=week, year=year)
        return render_template('index.html', events=events)
    except Exception as e:
        return render_template('index.html', events=[], error=str(e))


@app.route('/event/<int:id>')
def event_details(id):
    try:
        client = get_soap_client()
        event = client.service.getEventDetails(id=id)
        weather = None
        try:
            weather_client = Client(wsdl=WEATHER_WSDL_URL)
            weather = weather_client.service.getWeatherForDate(date=event.date)
        except Exception:
            pass
        return render_template('details.html', event=event, weather=weather)
    except Exception as e:
        return str(e)


@app.route('/download_pdf')
def download_pdf():
    try:
        client = get_soap_client()
        response = client.service.getEventSummaryPdf()
        pdf_bytes = response
        if isinstance(response, dict) and 'return' in response:
            pdf_bytes = response['return']
        if not isinstance(pdf_bytes, bytes):
            import base64
            pdf_bytes = base64.b64decode(pdf_bytes)
        return send_file(io.BytesIO(pdf_bytes), mimetype='application/pdf',
                         as_attachment=True, download_name='zestawienie.pdf')
    except Exception as e:
        return str(e)


@app.route('/suggest', methods=['GET', 'POST'])
def suggest():
    if request.method == 'POST':
        try:
            client = get_soap_client()
            client.service.submitProposal(
                name=request.form.get('name'),
                type=request.form.get('type'),
                date=request.form.get('date'),
                description=request.form.get('description'),
                organizerName=request.form.get('organizerName'),
                contactEmail=request.form.get('contactEmail')
            )
            flash('Propozycja zostala wyslana. Czeka na akceptacje administratora.')
            return redirect(url_for('suggest'))
        except Exception as e:
            flash(f'Blad: {e}')
    return render_template('suggest.html')


@app.route('/proposals')
@admin_required
def proposals():
    try:
        client = get_soap_client()
        items = client.service.getProposals()
        return render_template('proposals.html', proposals=items)
    except Exception as e:
        return render_template('proposals.html', proposals=[], error=str(e))


@app.route('/proposals/approve/<int:id>', methods=['POST'])
@admin_required
def approve_proposal(id):
    try:
        client = get_soap_client()
        client.service.approveProposal(id=id)
    except Exception as e:
        flash(f'Blad: {e}')
    return redirect(url_for('proposals'))


@app.route('/proposals/reject/<int:id>', methods=['POST'])
@admin_required
def reject_proposal(id):
    try:
        client = get_soap_client()
        client.service.rejectProposal(id=id)
    except Exception as e:
        flash(f'Blad: {e}')
    return redirect(url_for('proposals'))


def form_int(key):
    val = request.form.get(key, '').strip()
    return int(val) if val else 0


@app.route('/add', methods=['POST'])
@admin_required
def add_event():
    try:
        client = get_soap_client()
        client.service.addEvent(
            name=request.form.get('name'),
            type=request.form.get('type'),
            date=request.form.get('date'),
            week=form_int('week'),
            month=form_int('month'),
            year=form_int('year'),
            description=request.form.get('description')
        )
    except Exception as e:
        flash(f'Błąd dodawania: {e}')
    return redirect(url_for('index'))


@app.route('/update/<int:id>', methods=['POST'])
@admin_required
def update_event(id):
    try:
        client = get_soap_client()
        client.service.updateEvent(
            id=id,
            name=request.form.get('name'),
            type=request.form.get('type'),
            date=request.form.get('date'),
            week=form_int('week'),
            month=form_int('month'),
            year=form_int('year'),
            description=request.form.get('description')
        )
    except Exception as e:
        flash(f'Błąd aktualizacji: {e}')
    return redirect(url_for('index'))


@app.route('/delete/<int:id>', methods=['POST'])
@admin_required
def delete_event(id):
    try:
        client = get_soap_client()
        client.service.deleteEvent(id=id)
    except Exception as e:
        flash(f'Błąd usuwania: {e}')
    return redirect(url_for('index'))


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)
