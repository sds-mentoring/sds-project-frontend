# Simple placeholder Android Application

Write an Android application that has following functionalities:

1. At startup, it only an indigo button which says 'Send Request', placed at the
   center of the screen.

2. When the user press the button, send a `GET` request to 
   `10.0.2.2:3000/health` via HTTP. The server will respond a structured data if
   it succeeded to respond.

3. While waiting for the response, show a circle-shaped throbber spinning.

4. Upon reception of the request, show a popup screen which has small box in it
   and a button at the bottom. The contents of the box are the plain content of 
   the HTTP response body, in monospace font and with proper indentation -- as 
   mentioned above, the body will be a structured JSON data. The button, with
   white background and border, says 'Close'.

4-1. If the request was successful (status code 2XX), print the data in 
     light-green background with little green check sign at the top-left of it. 
     
4-2. If the request failed, print the data in light-red background with little
     red cross sign at the top-left of it.

5. If the user press the 'Close' button, get him back to the startup screen.

The application, although its main purpose is just to test basic 
functionalities, must be fancy. There should not be any out-of-frame text or
unaligned UI components. All components, except the small check or cross sign,
must be aligned at the center horizontally.

This directory holds basic skeleton of a 'birthday card project'. Modify 
whatever you want and add any dependency you want if needed. The application 
must be properly built with no compilation error or build error.
