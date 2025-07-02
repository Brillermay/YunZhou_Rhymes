import { ref } from 'vue';

const WS_URL = 'ws://192.168.142.49:8081/ws/game';

let socket: WebSocket;
const gameState = ref(Array(9).fill(''));

function connect() {
    socket = new WebSocket(WS_URL);

    socket.onopen = () => {
        console.log('WebSocket connected');
    };

    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.gameState) {
            gameState.value = data.gameState;
        }
    };

    socket.onclose = () => {
        console.log('WebSocket closed, retrying in 1s...');
        setTimeout(connect, 1000);
    };

    socket.onerror = (e) => {
        console.error('WebSocket error:', e);
        socket.close();
    };
}
connect();

const sendMove = (index: number, value: string) => {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ index, value }));
    } else {
        console.warn('WebSocket not open, move not sent');
    }
};

const resetGame = () => {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ reset: true }));
    } else {
        console.warn('WebSocket not open, reset not sent');
    }
};

export { gameState, sendMove, resetGame };