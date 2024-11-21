let canvas = document.getElementById('drawingCanvas');
let ctx = canvas.getContext('2d');
let drawing = false;
let color = '#000000';
let roomId = null;
let socket = null;
let eraserMode = false;
let num_players = 0;
let num_rounds = 0;
let nickName = '';

let prevX = 0, prevY = 0;


const myPromis = getUserInfo();
myPromis.then(result => {
    console.log(result);
    document.getElementById('nameProfile').textContent = result;
    const totalscore = getUserScore(result);
    totalscore.then(results => {
        console.log(results);
        document.getElementById('totalScore').textContent = results;
    }).catch(error =>{
        console.log(error);
    })
}).catch(error =>{
    console.log(error);
})









document.getElementById('createRoomBtn').addEventListener('click', createPage);
document.getElementById('startBtn').addEventListener('click', createRoom);
document.getElementById('joinRoomBtn').addEventListener('click', joinPage);
document.getElementById('startBtn2').addEventListener('click', joinRoom);
document.getElementById('sendChat').addEventListener('click', sendChatMessage);


document.getElementById('chatInput').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        sendChatMessage();
    }
});

function createPage() {
    document.getElementById('startBtn').style.display = 'block';
    document.getElementById('playersInput').style.display = 'block';
    document.getElementById('roundsInput').style.display = 'block';
    document.getElementById('joinRoomBtn').style.display = 'none';
    document.getElementById('createRoomBtn').style.display = 'none';
}

function joinPage() {
    document.getElementById('roomIdInput').style.display = 'block';
    document.getElementById('startBtn2').style.display = 'block';
    document.getElementById('joinRoomBtn').style.display = 'none';
    document.getElementById('createRoomBtn').style.display = 'none';
}

async function createRoom() {
    document.querySelector(".profile-container").style.display = "none";
    document.getElementById('nickname').style.display='block';
    document.getElementById('lamamais').style.display='block';
    document.getElementById('roomIdTmp').style.display='block';

    try {

        const username = await getUserInfo();
        if (!username) {
            console.log("Failed to retrieve username");
            return;
        }

        nickName = username;
        console.log("Authenticated user: ", nickName);

        num_players = parseInt(document.getElementById('playersInput').value.trim(), 10);
        num_rounds = parseInt(document.getElementById('roundsInput').value.trim(), 10);

        if (num_players <= 1 || num_rounds < 1 || isNaN(num_rounds) || isNaN(num_rounds) ) {
            alert('Please enter a nickname or a valid number of players and rounds.');
            return;
        }

        roomId = generateRoomId();
        connectToWebSocket(roomId);

        console.log(`Room ID: ${roomId}, Nickname: ${nickName}, Players: ${num_players}, Rounds: ${num_rounds}`);

        const roomRequestData = {
            roomId: roomId,
            player: nickName,
            numPlayers: num_players,
            numRounds: num_rounds
        };

        const response = await fetch('/api/addRoom', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(roomRequestData)
        });

        console.log("Response Status:", response.status);
        const data = await response.json();
        console.log("Response Data:", data);

        if (response.ok) {
            alert("Room created successfully!");
            hideRoomControls();
            displayGameCreation(roomId, nickName);
        } else {
            throw new Error("Network response was not ok.");
        }
    } catch (error) {
        console.error("Error creating room:", error);
        alert("An error occurred while creating the room.");
    }
}

function joinRoom() {
    document.querySelector(".profile-container").style.display = "none";

    getUserInfo().then(username => {
        if (username) {
            nickName = username;
            console.log("Authenticated user: ", username);
        } else {
            console.log("Failed to retrieve username");
            alert("Please log in first."); // Alert user to log in if username retrieval fails
            return;
        }

        const roomId = document.getElementById('roomIdInput').value.trim();
        if (roomId !== "") {
            checkRoomExistence(roomId, nickName)
                .then(exists => {
                    if (exists) {
                        connectToWebSocket(roomId);
                        // console.log()
                        displayGameDet(roomId, nickName);
                        displayRoomComponents(roomId, nickName);
                        hideRoomControls();

                        isFull(roomId)
                            .then(isRoomFull => {
                                // if(socket.readyState !== WebSocket.OPEN) {
                                //     connectToWebSocket(roomId);
                                // }
                                console.log(socket.readyState);
                                if (isRoomFull) {
                                    startGame(roomId);
                                }
                            });
                    } else {
                        isFull(roomId)
                            .then(exists => {
                                console.log(exists);
                                if (exists) {
                                    alert("Room is full, you cant join.");
                                } else {
                                    alert("room id is not valid, enter a valid room id. ");
                                }
                            })
                            .catch(error => {
                                console.error("error :", error);
                                alert("an error occurred");
                            })
                    }
                })
                .catch(error => {
                    console.error("Error checking room existence:", error);
                    alert("An error occurred while checking the room ID.");
                    document.querySelector(".profile-container").style.display = "block";
                    document.getElementById('totalScore').style.display="none";
                    document.getElementById('x').style.display="none";


                });
        } else {
            alert("Please enter a valid Room ID.");
        }
    });
}

function getUserInfo() {
    return fetch('/user-info', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(data => {
            console.log(data);
            return data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            return null;
        });
}

async function getUserScore(username) {
    const response = await fetch(`/user-score?username=${username}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }

    const totalScore = await response.json();
    console.log(totalScore);
    return totalScore;
}

function hideRoomControls() {
    document.getElementById('nickname').style.display='none';
    document.getElementById('lamamais').style.display='none';
    document.getElementById('roomIdTmp').style.display='none';

    document.getElementById('startBtn').style.display = 'none';
    document.getElementById('startBtn2').style.display = 'none';
    document.getElementById('playersInput').style.display = 'none';
    document.getElementById('roundsInput').style.display = 'none';
    document.getElementById('roomIdInput').style.display = 'none';
    document.getElementById('joinRoomBtn').style.display = 'none';
    document.getElementById('createRoomBtn').style.display = 'none';

}

function checkRoomExistence(roomId, nickName) {
    return fetch(`/api/checkRoom?roomId=${roomId}&newPlayer=${nickName}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Room check failed');
            }
            return response.json();
        })
        .then(data => data.exists);
}

function isFull(roomId) {
    return fetch(`/api/isFull?roomId=${roomId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Room check failed');
            }
            return response.json();
        })
        .then(data => data.exists);
}

function showCustomAlert(message, duration) {
    const alertDiv = document.createElement('div');

    alertDiv.innerText = message;

    alertDiv.style.position = 'fixed';
    alertDiv.style.left = '50%';
    alertDiv.style.top = '20px'; // Top of the screen
    alertDiv.style.transform = 'translateX(-50%)'; // Center horizontally
    alertDiv.style.padding = '30px 60px';
    alertDiv.style.backgroundColor = '#f44336';
    alertDiv.style.color = 'white';
    alertDiv.style.fontSize = '23px';
    alertDiv.style.borderRadius = '5px';
    alertDiv.style.zIndex = '1000'; // Ensures it's on top of everything
    alertDiv.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)';

    document.body.appendChild(alertDiv);

    setTimeout(function() {
        alertDiv.style.display = 'none';
        document.body.removeChild(alertDiv);
    }, duration);
}

function startGame(roomId) {
    if(socket.readyState === WebSocket.OPEN){
        socket.send(JSON.stringify({type: 'startGame', roomId: roomId}));
    } else {
        console.log("error error");
    }

}

function generateRoomId() {
    return 'room-' + Math.random().toString(36).substr(2, 9);
}

function displayGameDet(roomId, nickname) {
    document.getElementById('nickname').textContent = 'Nickname : ' + nickName;
    document.getElementById("roomIdTmp").value = roomId;
}

function displayGameCreation(roomId, nickName) {
    displayGameDet(roomId, nickName);
    displayRoomComponents();
}

function displayRoomComponents() {
    document.getElementById('nickname').style.display='block';
    document.getElementById('lamamais').style.display='block';
    document.getElementById('roomIdTmp').style.display='block';

    document.getElementById('drawingCanvas').style.display = 'block';
    document.getElementById('colorPicker').style.display = 'block';
    document.getElementById('clearCanvas').style.display = 'block';
    document.getElementById('eraserBtn').style.display = 'block';
    document.getElementById('chatBox').style.display = 'block';
    document.getElementById('chatInput').style.display = 'block';
    document.getElementById('sendChat').style.display = 'block';
    document.getElementById('messageBox').style.display = 'block';
    document.getElementById('scoreboard').style.display = 'block';
    document.getElementById('timer').style.display = 'block';


    const canvasElement = document.getElementById('drawingCanvas');

    canvasElement.style.pointerEvents = 'none';
    document.getElementById('colorPicker').disabled = true;
    document.getElementById('clearCanvas').disabled = true;
    document.getElementById('eraserBtn').disabled = true;
    document.getElementById('chatInput').disabled = true;
}

function connectToWebSocket(roomId) {
    if (socket !== null) {
        socket.close();
    }

    socket = new WebSocket("ws://" + window.location.host + "/draw");

    socket.onopen = function() {
        console.log("WebSocket connection established.");
        socket.send(JSON.stringify({type: 'join', roomId: roomId, nickname: nickName}));
    };

    socket.onmessage = function (event) {
        let data = JSON.parse(event.data);
        if (data.type === 'draw') {
            drawOnCanvas(data.prevX, data.prevY, data.x, data.y, data.color, data.lineWidth);
        } else if (data.type === 'clear') {
            clearCanvas();
        } else if (data.type === 'chat') {
            checkTheGuess(data.nickname, data.message);
            displayChatMessage(data.nickname, data.message);
        } else if (data.type === "system") {
            displaySystemMessage(data.message);
        } else if (data.type === "drawer") {
            alert("You are drawing the word: " + data.word);
        } else if (data.type === "timer") {
            console.log("Timer update received:", data);  // Debugging line
            document.getElementById('time').textContent = data.remainingTime;
        } else if (data.type==="alert"){
            showCustomAlert(data.message, 5000);
        } else if (data.type === 'clearChat') {
            clearChatBox();
        } else if (data.type === 'enableTools') {
            updateCanvasAccess();
        } else if (data.type === 'restartAccess') {
            restartCanvasAccess();
        } else if (data.type === 'enableChat') {
            enableChat();
        } else if (data.type === 'disableChat') {
            disableChat();
        } else if (data.type === 'validation') {
            onDrawingComplete(data.word, data.roomId);
        } else if (data.type === 'scoreboard') {
            const players = data.players;
            if (Array.isArray(players) && players.length > 0) {
                console.log("Players:", players);
                updateScoreboard(players);
            } else {
                console.error("No players found or players is not an array:", players);
            }
        }
    }
}

function enableChat() {
    document.getElementById('chatInput').disabled = false;
}

function disableChat() {
    document.getElementById('chatInput').disabled = true;
}

function updateCanvasAccess() {
    const canvasElement = document.getElementById('drawingCanvas');
    canvasElement.style.pointerEvents = 'auto';  // Enable drawing
    document.getElementById('colorPicker').disabled = false;
    document.getElementById('clearCanvas').disabled = false;
    document.getElementById('eraserBtn').disabled = false;
}

function restartCanvasAccess() {
    const canvasElement = document.getElementById('drawingCanvas');
    canvasElement.style.pointerEvents = 'none';  // Enable drawing
    document.getElementById('colorPicker').disabled = true;
    document.getElementById('clearCanvas').disabled = true;
    document.getElementById('eraserBtn').disabled = true;
}

function displaySystemMessage(message) {
    const messagesBox = document.getElementById("messages");
    const newMessage = document.createElement("p");
    newMessage.style.fontWeight = "bold";
    newMessage.textContent = message;
    messagesBox.appendChild(newMessage);
    messagesBox.scrollTop = messagesBox.scrollHeight;

    alert(message);
}

canvas.addEventListener('mousedown', (e) => {
    startDrawing(e.clientX, e.clientY);
});

canvas.addEventListener('mouseup', stopDrawing);
canvas.addEventListener('mousemove', (e) => {
    drawIfActive(e.clientX, e.clientY);
});

canvas.addEventListener('touchstart', (e) => {
    let touch = e.touches[0];
    startDrawing(touch.clientX, touch.clientY);
    e.preventDefault();
});

canvas.addEventListener('touchend', stopDrawing);
canvas.addEventListener('touchmove', (e) => {
    let touch = e.touches[0];
    drawIfActive(touch.clientX, touch.clientY);
    e.preventDefault();
});

document.getElementById('eraserBtn').addEventListener('click', function () {
    eraserMode = !eraserMode;
    this.textContent = eraserMode ? 'Eraser On' : 'Eraser Off';
});

document.getElementById('clearCanvas').addEventListener('click', function () {
    clearCanvas();
    socket.send(JSON.stringify({type: 'clear'}));
});

document.getElementById('colorPicker').addEventListener('change', function () {
    color = this.value;
});

function startDrawing(clientX, clientY) {
    drawing = true;
    let rect = canvas.getBoundingClientRect();
    prevX = clientX - rect.left;
    prevY = clientY - rect.top;
}

function stopDrawing() {
    drawing = false;
    ctx.beginPath();
}

function drawIfActive(clientX, clientY) {
    if (drawing) {
        let rect = canvas.getBoundingClientRect();
        let x = clientX - rect.left;
        let y = clientY - rect.top;

        let lineWidth = eraserMode ? 20 : 2;
        let drawColor = eraserMode ? '#FFFFFF' : color;

        sendDrawingEvent(prevX, prevY, x, y, drawColor, lineWidth);

        drawOnCanvas(prevX, prevY, x, y, drawColor, lineWidth);

        prevX = x;
        prevY = y;
    }
}

function sendDrawingEvent(prevX, prevY, x, y, color, lineWidth) {
    /*if(socket.readyState!==WebSocket.OPEN){
        connectToWebSocket(roomId);
        console.log("socket is reconnected!");
    }*/
    //if(socket.readyState===WebSocket.OPEN) {
        socket.send(JSON.stringify({
            type: 'draw',
            prevX: prevX,
            prevY: prevY,
            x: x,
            y: y,
            color: color,
            lineWidth: lineWidth
        }));
    //}
}

function drawOnCanvas(prevX, prevY, x, y, color, lineWidth) {
    ctx.strokeStyle = color;
    ctx.lineWidth = lineWidth;
    ctx.beginPath();
    ctx.moveTo(prevX, prevY);
    ctx.lineTo(x, y);
    ctx.stroke();
    ctx.closePath();
}

function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function sendChatMessage() {
    let chatInput = document.getElementById('chatInput');
    let message = chatInput.value.trim();
    if (message !== "") {
        let payload = JSON.stringify({type: 'chat', message: message, nickname: nickName});
        socket.send(payload);
        chatInput.value = '';
    }
}

function displayChatMessage(nickname, message) {
    let chatBox = document.getElementById('chatBox');
    let messageElement = document.createElement('p');
    messageElement.textContent = `${nickname}: ${message}`;
    chatBox.appendChild(messageElement);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function checkTheGuess(nickname, message){
    console.log(`${nickname}`);
    console.log(`${message}`);
    let x = document.getElementById("roomIdTmp").value;
    console.log("Retrieved Room ID:", x);

    const requestBody = `roomId=${encodeURIComponent(x)}&guess=${encodeURIComponent(message)}&username=${encodeURIComponent(nickname)}`;

    fetch('/api/guess', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: requestBody
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(data => {
            console.log('Success:', data);
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });
}

function clearChatBox() {
    const chatBox = document.getElementById("chatBox");
    chatBox.innerHTML = "";
}

function onDrawingComplete(word, roomID) {
    const drawingData = getDrawingData();

    validateDrawing(drawingData, word, roomID)
        .then(score => {
            console.log('Validation score:', score);
        })
        .catch(error => {
            console.error('Error validating drawing:', error);
            alert('There was an error validating your drawing. Please try again.');
        });
}

function getDrawingData() {
    const canvas = document.getElementById('drawingCanvas');
    return canvas.toDataURL();
}

async function validateDrawing(drawingData, word, roomID) {
    const response = await fetch('/api/validateDrawing', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            drawing: drawingData,
            word: word,
            roomID: roomID
        })
    });

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }

    const score = await response.json();
    return score;
}

function updateScoreboard(players) {
    const scoreboardElement = document.getElementById('score');
    scoreboardElement.innerHTML = '';

    players.forEach(player => {
        console.log(player.score);
        const playerElement = document.createElement('p');
        playerElement.textContent = `${player.name}: ${player.score}`;
        scoreboardElement.appendChild(playerElement);
    });
}



//////////////////
// Function to update the profile image when a new image is selected
function updateProfileImage() {
    const fileInput = document.getElementById('file-input');
    const profileImg = document.getElementById('profile-img');
    const file = fileInput.files[0];
    const reader = new FileReader();

    reader.onloadend = function () {
        profileImg.src = reader.result;
    }

    if (file) {
        reader.readAsDataURL(file);
    }
}

function showPlayOptions() {
    document.querySelector('.play-options').style.display = 'block';
    document.querySelector('.btn').style.display = 'none'; // Hide the Play button
}