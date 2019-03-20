from __future__ import print_function


import time
import calendar
import random
import boto3
import json


# --------------- Helpers that build all of the responses ----------------------


def build_speechlet_response(title, output, reprompt_text, should_end_session):
    return {
        'outputSpeech': {
            'type': 'PlainText',
            'text': output
        },
        'card': {
            'type': 'Simple',
            'title': "SessionSpeechlet - " + title,
            'content': "SessionSpeechlet - " + output
        },
        'reprompt': {
            'outputSpeech': {
                'type': 'PlainText',
                'text': reprompt_text
            }
        },
        'shouldEndSession': should_end_session
    }


def build_response(session_attributes, speechlet_response):
    return {
        'version': '1.0',
        'sessionAttributes': session_attributes,
        'response': speechlet_response
    }


# --------------- Functions that control the skill's behavior ------------------


def handle_session_start_request():
    session_attributes = {"idle" : True, "write" : False, "entry" : None, 
            "read" : False, "music" : False, "noise" : False, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    card_title = "Session Start"
    speech_output = "Welcome to Talk Sleepy To Me. What do you want to do?"
    reprompt_text = "You can ask what can I do for a list of things you can do."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, reprompt_text, should_end_session))


def write_in_journal(session):
    session_attributes = {"idle" : False, "write" : True, "entry" : "", 
            "read" : False, "music" : False, "noise" : False, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    card_title = "Starting New Journal Entry"
    speech_output = "Okay. Add to your journal one sentence at a time. " \
                    "Say new sentence before every sentence and say finish " \
                    "entry to finish journaling."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def am_writing(intent, session):
    session_attributes = session.get('attributes', {})
    
    card_title = "Writing In Journal"
    speech_output = "Got it. Remember to say finish entry to finish journaling."
    
    entry = session_attributes['entry']
    if entry != "":
        entry += " "
    entry += intent['slots']['Sentence']['value'].capitalize()
    entry += "."
    session_attributes['entry'] = entry
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def finish_entry(session):
    session_attributes = session.get('attributes', {})
    
    card_title = "Finishing Journal Entry"
    speech_output = "Okay. The journal is finished. Would you like to " \
                    "continue using the app?"
    
    dynamo = boto3.resource('dynamodb').Table('JournalEntries')
    dynamo.put_item(
        Item = {
            "Month" : int(time.strftime("%m")),
            "Day" : int(time.strftime("%d")),
            "Entry" : session_attributes['entry']
        }
    )
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def read_journal(intent, session):
    session_attributes = {"idle" : False, "write" : False, "entry" : None, 
            "read" : True, "music" : False, "noise" : False, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    if "value" in intent['slots']["Date"]:
        date = intent['slots']['Date']['value'].split("-")
        month = int(date[1])
        day = int(date[2])
    else:
        temp_month = intent['slots']['Month']['value'].lower()
        all_months = {
            "january": 1,
            "february": 2,
            "march": 3,
            "april": 4,
            "may": 5,
            "june": 6,
            "july": 7,
            "august": 8,
            "september": 9,
            "october": 10,
            "november": 11,
            "december": 12
        }
        month = all_months[temp_month]
        
        temp_day = intent['slots']['Day']['value']
        if len(temp_day) == 3:
            day = int(temp_day[0])
        else:
            day = int(temp_day[0:2])

    card_title = "Reading From Journal"
    
    dynamo = boto3.resource('dynamodb').Table('JournalEntries')
    res = dynamo.get_item(
        Key = {
            "Month" : month,
            "Day" : day
        }
    )
    
    if 'Item' in res:
        speech_output = "Okay. Reading journal from "
        speech_output += calendar.month_name[month]
        speech_output += " "
        speech_output += str(day)
        speech_output += ". "
        speech_output += res["Item"]["Entry"]
        speech_output += " Would you like to continue using the app?"
    else:
        speech_output = "I'm sorry. There is no journal entry for "
        speech_output += calendar.month_name[month]
        speech_output += " "
        speech_output += str(day)
        speech_output += "."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def read_no_date(session):
    session_attributes = session.get('attributes', {})

    card_title = "No Date"
    speech_output = "Alright. What journal entry do you want to read from?"
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def play_music(session):
    session_attributes = {"idle" : False, "write" : False, "entry" : None, 
            "read" : False, "music" : True, "noise" : False, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    card_title = "Playing Music"
    speech_output = "Okay. Say done when you want to stop playing music."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def play_noise(session):
    session_attributes = {"idle" : False, "write" : False, "entry" : None, 
            "read" : False, "music" : False, "noise" : True, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    card_title = "Playing Noise"
    speech_output = "Okay. Say done when you want to stop playing noise."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def restart_steps(session):
    session_attributes = session.get('attributes', {})
    session_attributes['index'] = 0
    
    card_title = "Restarting Steps"
    speech_output = "Okay. Restarting the steps. Please say next step to start."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def repeat_step(session):
    session_attributes = session.get('attributes', {})
    
    card_title = "Repeating Step"
    
    if (session_attributes['index'] == -1):
        speech_output = "Sorry. There is no step to repeat. Please say next " \
                        " step to start."
    else:
        speech_output = session_attributes['steps'][session_attributes['index']]
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))

def next_step(session):
    session_attributes = session.get('attributes', {})
    
    session_attributes['index'] = session_attributes['index'] + 1
    
    card_title = "Next Step"
    speech_output = session_attributes['steps'][session_attributes['index']]
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def prev_step(session):
    session_attributes = session.get('attributes', {})
    
    card_title = "Previous Step"
    
    if (session_attributes['index'] <= 0):
        speech_output = "Sorry. There is no previous step to say. Please say " \
                        "next step to start."
    else:
        session_attributes['index'] = session_attributes['index'] - 1
        speech_output = session_attributes['steps'][session_attributes['index']]
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def no_more_steps(session):
    session_attributes = session.get('attributes', {})
    
    card_title = "Done With Steps"
    speech_output = "There are no more steps. Would you like to continue " \
                    "using the app?"
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def breath_exercise(session):
    session_attributes = {"idle" : False, "write" : False, "entry" : None, 
            "read" : False, "music" : False, "noise" : False, 
            "exercise" : False, "index" : -1, "max" : None, "steps" : None,
            "end" : False}
    
    dynamo = boto3.resource('dynamodb').Table('BreathingExercises')
    res = dynamo.get_item(
        Key = {
            "Index" : random.randint(1, 4)
        }
    )
    session_attributes['steps'] = res["Item"]["Exercise"].split("\n")
    session_attributes['max'] = len(session_attributes['steps'])
    
    card_title = "Doing Breathing Exercises"
    speech_output = "Okay. Let's do a breathing exercise. Say next step " \
                    "to start."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
                card_title, speech_output, None, should_end_session))


def snore_exercise(session):
    session_attributes = {"idle" : False, "write" : False, "entry" : None, 
            "read" : False, "music" : False, "noise" : False, 
            "exercise" : True, "index" : -1, "max" : None, "steps" : None,
            "end" : False}
    
    dynamo = boto3.resource('dynamodb').Table('AntiSnoringExercises')
    res = dynamo.get_item(
        Key = {
            "Index" : random.randint(1, 4)
        }
    )
    session_attributes['steps'] = res["Item"]["Exercise"].split("\n")
    session_attributes['max'] = len(session_attributes['steps'])
    
    card_title = "Doing Snoring Exercises"
    speech_output = "Okay. Let's do an anti-snoring exercise. Say next step " \
                    "to start."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def cannot_perform_action(session):
    session_attributes = session.get('attributes', {})

    card_title = "Can't Do That"
    speech_output = "I'm sorry. I can't do that right now. You can ask what " \
                    "can I do if you need to."
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def continue_app(session):
    session_attributes = {"idle" : True, "write" : False, "entry" : None, 
            "read" : False, "music" : False, "noise" : False, 
            "exercise" : False, "index" : None, "max" : None, "steps" : None,
            "end" : False}
    
    card_title = "Start Over"
    speech_output = "Okay. What do you feel like doing?"
    
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


## Make these two the same message for now.
def leave_app():
    card_title = "Leaving The App"
    speech_output = "OK. Goodnight."
    should_end_session = True
    return build_response({}, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


## Make these two the same message for now.
def handle_session_end_request():
    card_title = "Session End"
    speech_output = "Goodnight."
    should_end_session = True
    return build_response({}, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def help_me(session):
    session_attributes = session.get('attributes', {})
    card_title = "Help"
    if session['attributes']['idle']:
        speech_output = "You can journal, read a previous journal entry,  " \
                        "listen to music or noise, do breathing exercises, " \
                        "and do anti-snoring exercises."
    elif session['attributes']['write']:
        speech_output = "How was your day? Journal one sentence at a time and" \
                        " say new sentence before every sentence. Say finish " \
                        "entry to finish journaling."
    elif session['attributes']['read']:
        speech_output = "Say read journal entry with the date you want to " \
                        "read from."
    elif session['attributes']['music']:
        speech_output = "Tell me what kind of music you want to listen to."
    elif session['attributes']['noise']:
        speech_output = "Tell me what kind of noise you want to listen to."
    elif session['attributes']['exercise']:
        speech_output = "You can ask for the next step, the previous step, " \
                        "restarting the steps, and repeating a step."

    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


# --------------- Events ------------------


def on_session_started(session_started_request, session):
    print("on_session_started requestId=" + session_started_request['requestId']
            + ", sessionId=" + session['sessionId'])


def on_launch(launch_request, session):
    print("on_launch requestId=" + launch_request['requestId'] +
            ", sessionId=" + session['sessionId'])
    return handle_session_start_request()


def on_intent(intent_request, session):
    print("on_intent requestId=" + intent_request['requestId'] +
            ", sessionId=" + session['sessionId'])

    intent = intent_request['intent']
    intent_name = intent_request['intent']['name']

    if (session['attributes']['end']):
        return handle_session_end_request()

    if intent_name == "WriteJournalIntent":
        return write_in_journal(session)
    elif intent_name == "NewSentenceIntent":
        return am_writing(intent, session)
    elif intent_name == "FinishEntryIntent":
        return finish_entry(session)
    elif intent_name == "ReadJournalIntent":
        return read_journal(intent, session)
    elif intent_name == "ReadNoDateIntent":
        return read_no_date(session)
    elif intent_name == "MusicIntent":
        return play_music(session)
    elif intent_name == "NoiseIntent":
        return play_noise(session)
    elif intent_name == "RestartIntent":
        if not session['attributes']['breath'] and not session['attributes']['snore']:
            return cannot_perform_action(session)
        return restart_steps(session)
    elif intent_name == "RepeatIntent":
        if not session['attributes']['breath'] and not session['attributes']['snore']:
            return cannot_perform_action(session)
        return repeat_step(session)
    elif intent_name == "NextIntent":
        if not session['attributes']['breath'] and not session['attributes']['snore']:
            return cannot_perform_action(session)
        if session['attributes']['index'] == session['attributes']['max'] - 1:
            return no_more_steps(session)
        return next_step(session)
    elif intent_name == "PrevIntent":
        if not session['attributes']['breath'] and not session['attributes']['snore']:
            return cannot_perform_action(session)
        return prev_step(session)
    elif intent_name == "BreathIntent":
        return breath_exercise(session)
    elif intent_name == "SnoreIntent":
        return snore_exercise(session)
    elif intent_name == "MainMenuIntent":
        return continue_app(session)
    elif intent_name == "EndIntent":
        return leave_app()
    elif intent_name == "HelpMeIntent":
        return help_me(session)


def on_session_ended(session_ended_request, session):
    print("on_session_ended requestId=" + session_ended_request['requestId'] +
            ", sessionId=" + session['sessionId'])


# --------------- Main handler ------------------


def respond(err, res=None):
    return {
        'statusCode': '400' if err else '200',
        'body': err.message if err else json.dumps(res),
        'headers': {
            'Content-Type': 'application/json', 'Access-Control-Allow-Headers': 'x-requested-with',
            "Access-Control-Allow-Origin" : "*", "Access-Control-Allow-Credentials" : True,
        },
    }


def lambda_handler(event, context):
    operations = {
        'DELETE': lambda dynamo, x: dynamo.delete_item(**x),
        'GET': lambda dynamo, x: dynamo.scan(**x),
        'POST': lambda dynamo, x: dynamo.put_item(**x),
        'PUT': lambda dynamo, x: dynamo.update_item(**x),
    }
    if 'httpMethod' in event:
        operation = event['httpMethod']
        if operation in operations:
            payload = event['queryStringParameters'] if operation == 'GET' else json.loads(event['body'])
            dynamo = boto3.resource('dynamodb').Table(payload['TableName'])
            return respond(None, operations[operation](dynamo, payload))


    print("event.session.application.applicationId=" +
          event['session']['application']['applicationId'])
          
    if (event['session']['application']['applicationId'] !=
            "amzn1.ask.skill.ede199a9-e6e8-4871-9315-58665a6768e7"):
        raise ValueError("Invalid Application ID")
          
    if event['session']['new']:
        on_session_started({'requestId': event['request']['requestId']},
                event['session'])
    
    if event['request']['type'] == "LaunchRequest":
        return on_launch(event['request'], event['session'])
    elif event['request']['type'] == "IntentRequest":
        return on_intent(event['request'], event['session'])
    elif event['request']['type'] == "SessionEndedRequest":
        return on_session_ended(event['request'], event['session'])
