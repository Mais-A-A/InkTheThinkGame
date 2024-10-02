
let canvas = document.getElementById('drawingCanvas');
let ctx = canvas.getContext('2d');
let drawing = false;
let color = '#000000';
let roomId = null;
let socket = null;
let eraserMode = false;  // Eraser mode toggle
let nickname = '';
let num_players = 0;
let num_rounds = 0;

let prevX = 0, prevY = 0;

// Event listeners for room creation and joining
document.getElementById('createRoomBtn').addEventListener('click', createRoom);
document.getElementById('joinRoomBtn').addEventListener('click', joinRoom);
document.getElementById('sendChat').addEventListener('click', sendChatMessage);


document.getElementById('chatInput').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        sendChatMessage(); // Call the sendMessage function when "Enter" is pressed
    }
});

function createRoom(){
    nickname = document.getElementById('nicknameInput').value.trim();
    num_players = parseInt(document.getElementById('playersInput').value.trim(), 10);
    num_rounds= parseInt(document.getElementById('roundsInput').value.trim(), 10);
    if (nickname === '' || num_players <= 1 || num_rounds < 1) {
        alert('Please enter a nickname or a valid num of player of a valid num of round.');
        return;
    }
    roomId = generateRoomId();
    connectToWebSocket(roomId);
    fetch(`/api/addRoom?roomId=${roomId}&player=${nickname}&numPlayers=${num_players}&numRounds=${num_rounds}`, {
        method: 'POST'
    })
        .then(response => {
            alert("Room created successfully!");
            hideRoomControls();
            displayGameCreation(roomId, nickname);
        })
        .catch(error => {
            console.error("Error creating room:", error);
            alert("An error occurred while creating the room.");
        });
}


function joinRoom(){
    nickname = document.getElementById('nicknameInput').value.trim();
    if (nickname === '') {
        alert('Please enter a nickname.');
        return;
    }

    const roomId = document.getElementById('roomIdInput').value.trim();
    if (roomId !== "") {
        checkRoomExistence(roomId, nickname)
            .then(exists => {
                if (exists) {
                    connectToWebSocket(roomId);  // Join the room if it exists
                    displayGameDet(roomId,nickname);// Show the room ID that the user joined
                    displayRoomComponents(roomId, nickname); //  added : display room components--> canvas,chatbox ..etc
                    hideRoomControls();

                    isFull(roomId)
                        .then(exists =>{
                            if (exists){
                                startGame();
                            }
                        })

                } else {
                    isFull(roomId)
                        .then(exists =>{
                            console.log(exists);
                            if (exists){
                                alert("Room is full, you cant join.");
                            }
                            else {
                                alert("room id is not valid, enter a valid room id. ");
                            }
                        })
                        .catch(error =>{
                            console.error("error :", error);
                            alert("an error occurred");
                        })

                }
            })
            .catch(error => {
                console.error("Error checking room existence:", error);
                alert("An error occurred while checking the room ID.");
            });
    } else {
        alert("Please enter a valid Room ID.");
    }
}

function hideRoomControls() {
    document.getElementById('nicknameInput').style.display = 'none';
    document.getElementById('createRoomBtn').style.display = 'none';
    document.getElementById('roomIdInput').style.display = 'none';
    document.getElementById('joinRoomBtn').style.display = 'none';
    document.getElementById('playersInput').style.display = 'none';
    document.getElementById('roundsInput').style.display = 'none';

}

function checkRoomExistence(roomId, nickname) {
    return fetch(`/api/checkRoom?roomId=${roomId}&newPlayer=${nickname}&numPlayers=${num_players}`)  // Make a request to the Spring Boot server
        .then(response => {
            if (!response.ok) {
                throw new Error('Room check failed');
            }
            return response.json();
        })
        .then(data => data.exists);
}

function isFull(roomId){
    return fetch(`/api/isFull?roomId=${roomId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Room check failed');
            }
            return response.json();
        })
        .then(data => data.exists);
}


// Generate a unique room ID
function generateRoomId() {
    return 'room-' + Math.random().toString(36).substr(2, 9);
}

function displayGameDet(roomId,nickname){
    document.getElementById('nickname').textContent = 'Nickname : ' + nickname;
    document.getElementById('currentRoom').textContent = 'Current Room ID: ' + roomId;
}

function displayGameCreation(roomId, nickname) {
    displayGameDet(roomId,nickname);
    displayRoomComponents();
}

// Display the current room ID and show all relevant controls
function displayRoomComponents() {
    document.getElementById('drawingCanvas').style.display = 'block';
    document.getElementById('colorPicker').style.display = 'block';
    document.getElementById('clearCanvas').style.display = 'block';
    document.getElementById('eraserBtn').style.display = 'block';
    document.getElementById('chatBox').style.display = 'block';
    document.getElementById('chatInput').style.display = 'block';
    document.getElementById('sendChat').style.display = 'block';
}

// Connect to WebSocket and join the specified room
function connectToWebSocket(roomId) {
    if (socket !== null) {
        socket.close(); // Close existing socket connection if any
    }
    socket = new WebSocket("ws://" + window.location.host + "/draw");
    // socket = new WebSocket("ws://localhost:8080/draw");

    // Once the connection is open, send the join room event with nickname
    socket.onopen = function () {
        socket.send(JSON.stringify({type: 'join', roomId: roomId, nickname: nickname}));
    };

    // Handle incoming messages (draw, clear, and chat events)
    socket.onmessage = function (event) {
        let data = JSON.parse(event.data);
        if (data.type === 'draw') {
            drawOnCanvas(data.prevX, data.prevY, data.x, data.y, data.color, data.lineWidth);
        } else if (data.type === 'clear') {
            clearCanvas();
        } else if (data.type === 'chat') {
            displayChatMessage(data.nickname, data.message);
        }
    };
}

// Handle drawing on the canvas
canvas.addEventListener('mousedown', (e) => {
    startDrawing(e.clientX, e.clientY);
});

canvas.addEventListener('mouseup', stopDrawing);
canvas.addEventListener('mousemove', (e) => {
    drawIfActive(e.clientX, e.clientY);
});

// Support touch events for mobile/tablet
canvas.addEventListener('touchstart', (e) => {
    let touch = e.touches[0];
    startDrawing(touch.clientX, touch.clientY);
    e.preventDefault();  // Prevent default touch behavior like scrolling
});

canvas.addEventListener('touchend', stopDrawing);
canvas.addEventListener('touchmove', (e) => {
    let touch = e.touches[0];
    drawIfActive(touch.clientX, touch.clientY);
    e.preventDefault();  // Prevent default touch behavior like scrolling
});

// Eraser toggle
document.getElementById('eraserBtn').addEventListener('click', function () {
    eraserMode = !eraserMode;
    this.textContent = eraserMode ? 'Eraser On' : 'Eraser Off';
});

// Clear canvas button
document.getElementById('clearCanvas').addEventListener('click', function () {
    clearCanvas();
    socket.send(JSON.stringify({type: 'clear'}));
});

// Update color from color picker
document.getElementById('colorPicker').addEventListener('change', function () {
    color = this.value;
});

// Start drawing
function startDrawing(clientX, clientY) {
    drawing = true;
    let rect = canvas.getBoundingClientRect();
    prevX = clientX - rect.left;
    prevY = clientY - rect.top;
}

// Stop drawing
function stopDrawing() {
    drawing = false;
    ctx.beginPath();  // Reset the path
}

// Draw if the user is actively drawing
function drawIfActive(clientX, clientY) {
    if (drawing) {
        let rect = canvas.getBoundingClientRect();
        let x = clientX - rect.left;
        let y = clientY - rect.top;

        let lineWidth = eraserMode ? 20 : 2;  // Eraser is wider
        let drawColor = eraserMode ? '#FFFFFF' : color;  // White for eraser, selected color otherwise

        // Send both previous and current points to draw a smooth line
        sendDrawingEvent(prevX, prevY, x, y, drawColor, lineWidth);

        // Draw locally
        drawOnCanvas(prevX, prevY, x, y, drawColor, lineWidth);

        // Update previous coordinates
        prevX = x;
        prevY = y;
    }
}

// Send drawing event to the server
function sendDrawingEvent(prevX, prevY, x, y, color, lineWidth) {
    socket.send(JSON.stringify({
        type: 'draw',
        prevX: prevX,
        prevY: prevY,
        x: x,
        y: y,
        color: color,
        lineWidth: lineWidth
    }));
}

// Draw on canvas from (prevX, prevY) to (x, y)
function drawOnCanvas(prevX, prevY, x, y, color, lineWidth) {
    ctx.strokeStyle = color;
    ctx.lineWidth = lineWidth;
    ctx.beginPath();
    ctx.moveTo(prevX, prevY);  // Move to the previous point
    ctx.lineTo(x, y);          // Draw to the current point
    ctx.stroke();
    ctx.closePath();           // Close the path
}

// Clear the canvas
function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

// Handle sending chat messages
function sendChatMessage() {
    let chatInput = document.getElementById('chatInput');
    let message = chatInput.value.trim();
    if (message !== "") {
        let payload = JSON.stringify({type: 'chat', message: message, nickname: nickname});
        socket.send(payload);
        chatInput.value = ''; // Clear input field
    }
}

// Display chat message in the chat box
function displayChatMessage(nickname, message) {
    let chatBox = document.getElementById('chatBox');
    let messageElement = document.createElement('p');
    messageElement.textContent = `${nickname}: ${message}`;
    chatBox.appendChild(messageElement);
    chatBox.scrollTop = chatBox.scrollHeight; // Scroll to the bottom
}