const connect = () => {
    var socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

connect();

const onConnected = () => {
    setUserData({ ...userData, "connected": true });
    stompClient.subscribe('/chatroom/public ', onMessageReceived);
    stompClient.subscribe('/user/' + userData.username + '/private', onPrivateMessage);
    userJoin();
}

const checkout = () => {
    event.preventDefault();
    sock.send('Hello, Server!');
    console.log("a");
}

