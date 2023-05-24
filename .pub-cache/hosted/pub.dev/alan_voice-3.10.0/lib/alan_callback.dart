import 'dart:convert';

class ScriptCallback {
  late String method;
  late String body;
  late String error;

  ScriptCallback(String method, String body, String error) {
    this.method = method;
    this.body = body;
    this.error = error;
  }

  @override
  String toString() {
    return "Method: $method, body: $body, error: $error";
  }
}

class CommandCallback {
  late Map<String, dynamic> data;

  CommandCallback(String payload) {
    data = jsonDecode(payload);
  }

  @override
  String toString() {
    return data.toString();
  }
}

class EventCallback {
  late String name;
  late Map<String, dynamic> data;

  EventCallback(String event, String payload) {
    name = event;
    data = jsonDecode(payload);
  }

  @override
  String toString() {
    return data.toString();
  }
}

class ButtonStateCallback {
  late String stateName;

  ButtonStateCallback(String newState) {
    stateName = newState;
  }
}

class Command {
  late Map<String, dynamic> data;

  Command(String payload) {
    data = jsonDecode(payload);
  }

  @override
  String toString() {
    return data.toString();
  }
}

class Event {
  late Map<String, dynamic> data;

  Event(String payload) {
    data = jsonDecode(payload);
  }

  @override
  String toString() {
    return data.toString();
  }
}

class ButtonState {
  late String name;

  ButtonState(String state) {
    name = state;
  }
}
