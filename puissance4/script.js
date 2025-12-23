// Configuration du jeu
const ROWS = 6;
const COLS = 7;
const WINNING_LENGTH = 4;

// État du jeu
let board = [];
let currentPlayer = 1;
let gameOver = false;
let moveHistory = [];
let scores = { player1: 0, player2: 0, draw: 0 };

// Éléments DOM
const gameBoard = document.getElementById('game-board');
const currentPlayerDisplay = document.getElementById('current-player');
const winnerModal = document.getElementById('winner-modal');
const winnerMessage = document.getElementById('winner-message');
const restartBtn = document.getElementById('restart-btn');
const undoBtn = document.getElementById('undo-btn');
const playAgainBtn = document.getElementById('play-again-btn');
const score1Display = document.getElementById('score-1');
const score2Display = document.getElementById('score-2');
const scoreDrawDisplay = document.getElementById('score-draw');

// Initialisation du jeu
function initGame() {
    board = Array(ROWS).fill(null).map(() => Array(COLS).fill(0));
    currentPlayer = 1;
    gameOver = false;
    moveHistory = [];
    renderBoard();
    updateTurnDisplay();
    winnerModal.classList.add('hidden');
}

// Rendu du plateau
function renderBoard() {
    gameBoard.innerHTML = '';

    for (let col = 0; col < COLS; col++) {
        const column = document.createElement('div');
        column.className = 'column';
        column.dataset.col = col;

        for (let row = 0; row < ROWS; row++) {
            const cell = document.createElement('div');
            cell.className = 'cell';
            cell.dataset.row = row;
            cell.dataset.col = col;

            if (board[row][col] === 1) {
                cell.classList.add('red');
            } else if (board[row][col] === 2) {
                cell.classList.add('yellow');
            }

            column.appendChild(cell);
        }

        column.addEventListener('click', () => handleColumnClick(col));
        gameBoard.appendChild(column);
    }
}

// Gestion du clic sur une colonne
function handleColumnClick(col) {
    if (gameOver) return;

    const row = getAvailableRow(col);
    if (row === -1) return; // Colonne pleine

    // Placer le jeton
    board[row][col] = currentPlayer;
    moveHistory.push({ row, col, player: currentPlayer });

    // Mettre à jour l'affichage
    const cell = document.querySelector(`.cell[data-row="${row}"][data-col="${col}"]`);
    cell.classList.add(currentPlayer === 1 ? 'red' : 'yellow');

    // Vérifier la victoire
    const winningCells = checkWin(row, col);
    if (winningCells) {
        gameOver = true;
        highlightWinningCells(winningCells);
        scores[`player${currentPlayer}`]++;
        updateScores();
        setTimeout(() => showWinner(`Joueur ${currentPlayer} gagne!`), 600);
        return;
    }

    // Vérifier l'égalité
    if (checkDraw()) {
        gameOver = true;
        scores.draw++;
        updateScores();
        setTimeout(() => showWinner('Égalité!'), 300);
        return;
    }

    // Changer de joueur
    currentPlayer = currentPlayer === 1 ? 2 : 1;
    updateTurnDisplay();
}

// Trouver la ligne disponible dans une colonne
function getAvailableRow(col) {
    for (let row = ROWS - 1; row >= 0; row--) {
        if (board[row][col] === 0) {
            return row;
        }
    }
    return -1;
}

// Vérifier la victoire
function checkWin(row, col) {
    const player = board[row][col];
    const directions = [
        { dr: 0, dc: 1 },   // Horizontal
        { dr: 1, dc: 0 },   // Vertical
        { dr: 1, dc: 1 },   // Diagonale descendante
        { dr: 1, dc: -1 }   // Diagonale montante
    ];

    for (const { dr, dc } of directions) {
        const cells = [{ row, col }];

        // Vérifier dans une direction
        for (let i = 1; i < WINNING_LENGTH; i++) {
            const newRow = row + dr * i;
            const newCol = col + dc * i;
            if (isValidCell(newRow, newCol) && board[newRow][newCol] === player) {
                cells.push({ row: newRow, col: newCol });
            } else {
                break;
            }
        }

        // Vérifier dans la direction opposée
        for (let i = 1; i < WINNING_LENGTH; i++) {
            const newRow = row - dr * i;
            const newCol = col - dc * i;
            if (isValidCell(newRow, newCol) && board[newRow][newCol] === player) {
                cells.push({ row: newRow, col: newCol });
            } else {
                break;
            }
        }

        if (cells.length >= WINNING_LENGTH) {
            return cells;
        }
    }

    return null;
}

// Vérifier si une cellule est valide
function isValidCell(row, col) {
    return row >= 0 && row < ROWS && col >= 0 && col < COLS;
}

// Vérifier l'égalité
function checkDraw() {
    return board[0].every(cell => cell !== 0);
}

// Mettre en évidence les cellules gagnantes
function highlightWinningCells(cells) {
    cells.forEach(({ row, col }) => {
        const cell = document.querySelector(`.cell[data-row="${row}"][data-col="${col}"]`);
        cell.classList.add('winning');
    });
}

// Afficher le gagnant
function showWinner(message) {
    winnerMessage.textContent = message;
    winnerModal.classList.remove('hidden');
}

// Mettre à jour l'affichage du tour
function updateTurnDisplay() {
    currentPlayerDisplay.textContent = `Tour du Joueur ${currentPlayer}`;
    currentPlayerDisplay.style.color = currentPlayer === 1 ? '#ff6b6b' : '#ffd93d';
}

// Mettre à jour les scores
function updateScores() {
    score1Display.textContent = scores.player1;
    score2Display.textContent = scores.player2;
    scoreDrawDisplay.textContent = scores.draw;
}

// Annuler le dernier coup
function undoMove() {
    if (moveHistory.length === 0 || gameOver) return;

    const lastMove = moveHistory.pop();
    board[lastMove.row][lastMove.col] = 0;

    const cell = document.querySelector(`.cell[data-row="${lastMove.row}"][data-col="${lastMove.col}"]`);
    cell.classList.remove('red', 'yellow', 'winning');

    currentPlayer = lastMove.player;
    updateTurnDisplay();
}

// Événements
restartBtn.addEventListener('click', initGame);
undoBtn.addEventListener('click', undoMove);
playAgainBtn.addEventListener('click', initGame);

// Lancer le jeu
initGame();
