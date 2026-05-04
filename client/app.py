from flask import Flask, render_template, request, redirect, url_for, send_file
from zeep import Client
from zeep.transports import Transport
import requests
import io
import urllib3
import os

# Suppress insecure request warnings for self-signed certs
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

app = Flask(__name__)

# WSDL URL for the Java Web Service
WSDL_URL = os.getenv('EVENTS_WSDL_URL', 'https://localhost:8443/ws/events?wsdl')
WEATHER_WSDL_URL = os.getenv('WEATHER_WSDL_URL', 'http://localhost:8444/ws/weather?wsdl')

def get_soap_client():
    # Disable SSL verification for local self-signed certificate
    session = requests.Session()
    session.verify = False
    transport = Transport(session=session)
    # Enable MTOM to receive the PDF as binary attachment
    # Zeep supports MTOM automatically in newer versions when parsing responses
    client = Client(wsdl=WSDL_URL, transport=transport)
    return client

@app.route('/')
def index():
    try:
        client = get_soap_client()
        # Initial call to get events for a default week or all?
        # Actually, let's fetch all via some method, but we only have day, week, details.
        # Let's get events for the current week as default.
        events = client.service.getAllEvents()
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

@app.route('/add', methods=['POST'])
def add_event():
    name = request.form.get('name')
    type_ = request.form.get('type')
    date = request.form.get('date')
    week = int(request.form.get('week'))
    month = int(request.form.get('month'))
    year = int(request.form.get('year'))
    description = request.form.get('description')
    
    try:
        client = get_soap_client()
        client.service.addEvent(
            name=name, type=type_, date=date, 
            week=week, month=month, year=year, description=description
        )
        return redirect(url_for('index'))
    except Exception as e:
        return str(e)

@app.route('/update/<int:id>', methods=['POST'])
def update_event(id):
    name = request.form.get('name')
    type_ = request.form.get('type')
    date = request.form.get('date')
    week = int(request.form.get('week'))
    month = int(request.form.get('month'))
    year = int(request.form.get('year'))
    description = request.form.get('description')
    
    try:
        client = get_soap_client()
        client.service.updateEvent(
            id=id, name=name, type=type_, date=date, 
            week=week, month=month, year=year, description=description
        )
        return redirect(url_for('index'))
    except Exception as e:
        return str(e)

@app.route('/download_pdf')
def download_pdf():
    try:
        client = get_soap_client()
        # Call the MTOM endpoint
        response = client.service.getEventSummaryPdf()
        # Since zeep handles MTOM, the response is typically bytes if MTOM decoded
        # Depending on zeep version and wsdl, it could be inside a specific return object
        # In this simple case, the return is a base64 encoded string or raw bytes
        # Let's handle both
        pdf_bytes = response
        if isinstance(response, dict) and 'return' in response:
            pdf_bytes = response['return']
            
        if not isinstance(pdf_bytes, bytes):
            # Try to decode if it's somehow base64 string
            import base64
            pdf_bytes = base64.b64decode(pdf_bytes)
            
        return send_file(
            io.BytesIO(pdf_bytes),
            mimetype='application/pdf',
            as_attachment=True,
            download_name='zestawienie.pdf'
        )
    except Exception as e:
        return str(e)

if __name__ == '__main__':
    # Running local dev server
    app.run(host='0.0.0.0', debug=True, port=5000)
