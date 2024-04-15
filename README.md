# CS3524 Team Lima Messenger Application

## Dependencies

`java`
`make`

## Setup:

Clone the repository:

`git clone https://github.com/cs3524-lima/messenger-assessment.git`

Navigate to the directory:

`cd messenger-assessment/messenger`

### Compiling:

Compile the server and client files using the makefile:

`make`

### Running:

#### Run the server:
`java server/StartServer`

#### Run the client:
`java client/StartClient`

The client must be running at the same time as the server.

While both server and client are running, you will be able to send messages through the client. More than one instance of the client can run and they will be able to relay messages to each other through the server.
