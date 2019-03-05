#  Author: Andrew Huynh, SID 25607104, cs160-aau

from __future__ import print_function


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
    session_attributes = {"idle" : True, "inCPR" : False, "turn" : False, 
            "ready" : False, "firstChest" : True, "firstBreath" : True, 
            "stop?" : False}
    card_title = "Session Start"
    speech_output = "Emergency First Aid here, what can I help you with? " \
                    "Please remember to stay calm."
    reprompt_text = "Please tell me your what you're emergency is. You can " \
                    "ask me 'what can I say' or 'help' for a list of " \
                    "emergencies that I can respond to."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, reprompt_text, should_end_session))


def handle_session_end_request():
    card_title = "Session End"
    speech_output = "Don't forget to call 911, as well! I hope everything " \
                    "goes well. Goodbye."
    should_end_session = True
    return build_response({}, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def help_me(session):
    session_attributes = session.get('attributes', {})
    card_title = "Help"
    if session['attributes']['idle']:
        speech_output = "I can help with checking an injured adult, checking " \
                        "an ill adult, conscious choking, unconscious " \
                        "choking, CPR, AED for an adult, AED for an older " \
                        "child, controlling external bleeding, burns, " \
                        "poisoning, neck injuries, spinal injuries, and stroke."
    elif session['attributes']['inCPR']:
        speech_output = "I can tell you how to do chest compressions, tell " \
                        "you how to do rescue breaths, restart at the chest " \
                        "compressions, restart at the rescue breaths, and " \
                        "stop CPR."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def respond_to_emergency(intent, session):
    emergency = intent['slots']['Emergency']['value']
    if emergency == "checking an injured adult":
        return not_implemented_yet()
    elif emergency == "checking an ill adult":
        return not_implemented_yet()
    elif emergency == "choking":
        return not_implemented_yet()
    elif emergency == "conscious choking":
        return not_implemented_yet()
    elif emergency == "unconscious choking":
        return not_implemented_yet()
    elif emergency == "CPR":
        session['attributes']['idle'] = False
        session['attributes']['inCPR'] = True
        return do_compressions_before(session)
    elif emergency == "AED":
        return not_implemented_yet()
    elif emergency == "AED for an adult":
        return not_implemented_yet()
    elif emergency == "AED for an older child":
        return not_implemented_yet()
    elif emergency == "controlling bleeding":
        return not_implemented_yet()
    elif emergency == "controlling external bleeding":
        return not_implemented_yet()
    elif emergency == "burns":
        return not_implemented_yet()
    elif emergency == "poisoning":
        return not_implemented_yet()
    elif emergency == "neck injuries":
        return not_implemented_yet()
    elif emergency == "spinal injuries":
        return not_implemented_yet()
    elif emergency == "stroke":
        return not_implemented_yet()


def do_compressions_before(session):
    session_attributes = session.get('attributes', {})
    card_title = "Do Chest Compressions Before"
    if session['attributes']['firstChest']:
        session_attributes['firstChest'] = False
        speech_output = "Give thirty chest compressions. To do chest " \
                        "compressions, lie the person down on firm, flat " \
                        "surface if possible. Now, push hard and fast in the " \
                        "middle of the chest at least two inches deep and at " \
                        "a rate of at least one hundred compressions per " \
                        "minute. When you are ready to begin, say 'ready'."
    else:
        speech_output = "Give thirty chest compressions. When you are ready " \
                        "to begin, say 'ready'."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))
            
            
def do_compressions_after(session):
    session_attributes = session.get('attributes', {})
    card_title = "Do Chest Compressions After"
    speech_output = "When you are done with thirty compressions, say 'done'."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def explain_compressions(session):
    session_attributes = session.get('attributes', {})
    card_title = "Explain Chest Compressions"
    speech_output = "To do chest compressions, lie the person down on firm, " \
                    "flat surface if possible. Now, push hard and fast in " \
                    "the middle of the chest at least two inches deep and at " \
                    "a rate of at least one hundred compressions per minute."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def do_breaths_before(session):
    session_attributes = session.get('attributes', {})
    card_title = "Do Rescue Breaths"
    if session['attributes']['firstBreath']:
        session_attributes['firstBreath'] = False
        speech_output = "Give two rescue breaths. To do resuce breaths, " \
                        "first tilt the person's head back and lift the chin " \
                        "up. Next pinch the nose shut then make a complete " \
                        "seal over the person's mouth. Blow in the person's " \
                        "mouth for about one second to make the chest " \
                        "clearly rise. If the chest does not rise with the " \
                        "rescue breaths, retilt the head and continue giving " \
                        "rescue breaths. When you are ready to begin, say " \
                        "'ready'."
    else:
        speech_output = "Give two rescue breaths. When you are ready to " \
                        "begin, say 'ready'."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))
            
            
def do_breaths_after(session):
    session_attributes = session.get('attributes', {})
    card_title = "Do Rescue Breaths"
    speech_output = "When you are done with two rescue breaths, say 'done'."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))
    
    
def explain_breaths(session):
    session_attributes = session.get('attributes', {})
    card_title = "Explain Rescue Breaths"
    speech_output = "To do resuce breaths, first tilt the person's head back " \
                    "and lift the chin up. Next pinch the nose shut then " \
                    "make a complete seal over the person's mouth. Blow in " \
                    "the person's mouth for about one second to make the " \
                    "chest clearly rise. If the chest does not rise with the " \
                    "rescue breaths, retilt the head and continue giving " \
                    "rescue breaths."
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def stop(session):
    if session['attributes']['idle']:
        return handle_session_start_request()
    session_attributes = session.get('attributes', {})
    session_attributes['stop?'] = True
    card_title = "Confirm Stop"
    speech_output = "Are you sure you want to stop?"
    should_end_session = False
    return build_response(session_attributes, build_speechlet_response(
            card_title, speech_output, None, should_end_session))


def yes_stop(session):
    if session['attributes']['idle']:
        return handle_session_start_request()
    if session['attributes']['stop?']:
        return handle_session_end_request()
    if session['attributes']['inCPR']:
        if session['attributes']['turn']:
            if session['attributes']['ready']:
                return do_breaths_after(session)
            return do_breaths_before(session)
        if session['attributes']['ready']:
            return do_compressions_after(session)
        return do_compressions_before(session)

def no_stop(session):
    if session['attributes']['idle']:
        return handle_session_start_request()
    if session['attributes']['inCPR']:
        if session['attributes']['turn']:
            if session['attributes']['ready']:
                return do_breaths_after(session)
            return do_breaths_before(session)
        if session['attributes']['ready']:
            return do_compressions_after(session)
        return do_compressions_before(session)

    
def ready(session):
    if session['attributes']['idle']:
        return handle_session_start_request()
    session['attributes']['ready'] = True
    if session['attributes']['inCPR']:
        if session['attributes']['turn']:
            return do_breaths_after(session)
        return do_compressions_after(session)
    
    
def done(session):
    if session['attributes']['idle']:
        return handle_session_start_request()
    if session['attributes']['inCPR']:
        if not session['attributes']['ready']:
            if session['attributes']['turn']:
                return do_breaths_before(session)
            return do_compressions_before(session)
        session['attributes']['turn'] = not session['attributes']['turn']
        session['attributes']['ready'] = False
        if session['attributes']['turn']:
            return do_breaths_before(session)
        return do_compressions_before(session)


def not_implemented_yet():
    card_title = "Not Implemented Yet"
    speech_output = "Call 911 for assistance! I hope everything goes well. " \
                    "Goodbye."
    should_end_session = True
    return build_response({}, build_speechlet_response(
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

    if intent_name == "EmergencyIntent":
        return respond_to_emergency(intent, session)
    elif intent_name == "HelpMeIntent":
        return help_me(session)
    elif intent_name == "InjuredAdultIntent":
        return not_implemented_yet()
    elif intent_name == "IllAdultIntent":
        return not_implemented_yet()
    elif intent_name == "ChokingIntent":
        return not_implemented_yet()
    elif intent_name == "CPRIntent":
        session['attributes']['idle'] = False
        session['attributes']['inCPR'] = True
        return do_compressions_before(session)
    elif intent_name == "AEDIntent":
        return not_implemented_yet()
    elif intent_name == "BleedingIntent":
        return not_implemented_yet()
    elif intent_name == "BurnsIntent":
        return not_implemented_yet()
    elif intent_name == "PoisonIntent":
        return not_implemented_yet()
    elif intent_name == "NeckInjuryIntent":
        return not_implemented_yet()
    elif intent_name == "SpinalInjuryIntent":
        return not_implemented_yet()
    elif intent_name == "StrokeIntent":
        return not_implemented_yet()
    elif intent_name == "ConsciousIntent":
        return not_implemented_yet()
    elif intent_name == "UnconsciousIntent":
        return not_implemented_yet()
    elif intent_name == "ExplainCompressionsIntent":
        return explain_compressions(session)
    elif intent_name == "ExplainBreathsIntent":
        return explain_breaths(session)
    elif intent_name == "RestartCompressionsIntent":
        if session['attributes']['inCPR']:
            session['attributes']['turn'] = False
            session['attributes']['ready'] = False
            return do_compressions_before(session)
        return handle_session_start_request()
    elif intent_name == "RestartBreathsIntent":
        if session['attributes']['inCPR']:
            session['attributes']['turn'] = True
            session['attributes']['ready'] = False
            return do_breaths_before(session)
        return handle_session_start_request()
    elif intent_name == "StopIntent":
        return stop(session)
    elif intent_name == "YesStopIntent":
        return yes_stop(session)
    elif intent_name == "NoStopIntent":
        return no_stop(session)
    elif intent_name == "ReadyIntent":
        return ready(session)
    elif intent_name == "DoneIntent":
        return done(session)
    else:
        raise ValueError("Invalid intent")


def on_session_ended(session_ended_request, session):
    print("on_session_ended requestId=" + session_ended_request['requestId'] +
            ", sessionId=" + session['sessionId'])


# --------------- Main handler ------------------


def lambda_handler(event, context):
    print("event.session.application.applicationId=" +
          event['session']['application']['applicationId'])

    if (event['session']['application']['applicationId'] !=
            "amzn1.ask.skill.2f97e79e-dc55-4f80-953d-d26255075816"):
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
