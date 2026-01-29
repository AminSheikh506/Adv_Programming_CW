# Adv_Programming_CW
BY:
- Amin SHEIKH
- Kipp SUMMERS
- Jahid EMON
- Hugo PIPER

## Task Distribution

HUGO PIPER:
  Create "Create Server" functionality via the CLI and inform the server creator that they have been assigned the role of "coordinator"
  Create "Join Server via IP and PORT functionality"
  Establish coordinator logic incase the server admin (ex-coordinator) leaves the server:
    - This will work by having the coordinator send a heartbeat signal to each user every couple seconds.
    - If the coordinators heartbeat stops, use a "Bully" algorithm to choose the new host based on who is the oldest member in the chatroom (use a LIFO queue datastructure).
    - All the other users will proceed by connecting to the new coordinators IP and PORT automatically.

AMIN SHEIKH:
  Create a system where the client will be informed about who the current coordinator is via the CLI upon joining a server (Emon will add this to the GUI later)
  Create a system where the user can input their ID (username) before connecting to the server.
  - All the clients ID's MUST be different from one another.
  - A member who tries to join using the same ID as someone else should be rejected.
  Create system where clients can request to view all the members details from the coordniator via the CLI (emon will encorprate this into the GUI later):
  - ID's
  - IP ADDRESSES
  - PORTS

KIPP SUMMERS:
  Create multithreading system so the server can handle multiple clients with ease:
  - A thread pool with 1 thread allocated to each client could be a good start. Thread pool size shouldn't realistically need to exceed 10
  - Ensure threads are managed properly and discarded of when a client leaves
  - Ensure thread safety and the absence of possible race conditions
  Optimize code and ensure all systems are running efficiently and threads are encorprated properly within the code
  Conduct JUnit based testing and fault tolerance testing once the code and GUI is complete.

JAHID EMON:
Create the homepage GUI:
- Have an initial GUI where you can choose to A) Create server B) Join server Via IP ADDRESS and PORT
- This is where the clients should put in their ID's (username). If a user tries to connect with an ID already in use, the ID should be rejected upon attempting to connect.
- The homepage GUI must have an EXIT button which terminates the application.
Create the chatroom GUI:
- The chatroom must have a button where clients can request the IDs, IP's and PORTS from the coordinator which will appear in a seperate box.
- The chatroom must have a main chatbox where clients can see the messages.
- The chatroom must have a section which alerts clients if they are the coordinator of the server.
- The chatroom must have an EXIT button which brings the client back to the homepage GUI
- The chatroom must alert the client in the event that the coordinator changes OR the client has lost connection.

## Additional notes:
There will NOT be a seperate client and server application. Since any client can become the server (coordinator) at any time, the client/server script should be encorprated into the same package.

This project does NOT need to operate outside of the private network. IP addresses are expected to be private IP's (e.g 192.168.1.69) and not public.
- When time comes to show our project to the tutor to be graded, all the clients and the server will be running on the SAME machine. In this case, use the loopback IP address 127.0.0.1 
