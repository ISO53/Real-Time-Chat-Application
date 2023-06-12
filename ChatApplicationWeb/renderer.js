// ******************** Declare Variables ********************
// Imports
const net = require("net");
const client = new net.Socket();
const fs = require("fs");

// Socket settings
const IP_ADDRESS = "127.0.0.1";
const PORT = 8080;

// Javascript variables
var USER_NAME = "";
var CURR_CHAT = "";

// Message types
const TYPE_TEXT = 1;
const TYPE_FILE = 2;

// File types
const fileTypesMap = {
	"image/png": 0,
	"image/jpeg": 1,
	"image/jpg": 1,
	"application/pdf": 3,
	"application/msword": 4,
	"application/vnd.openxmlformats-officedocument.wordprocessingml.document": 5,
	"text/plain": 6,
	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": 7,
};

// Popup message types
const TYPE_INFO = 0;
const TYPE_WARNING = 1;
const TYPE_ERROR = 2;

// Socket communication types
const MESSAGE_END = "######";
const REGISTER = "0";
const ALREADY_EXIST = "1";
const LOGIN = "2";
const DOESNT_EXIST = "3";
const WRONG_PASSWORD = "4";
const GET_USERS = "5";
const PERMISSION_DENIED = "6";
const SUCCESSFUL_REGISTER = "7";
const SUCCESSFUL_LOGIN = "8";
const GET_CHATS = "9";
const GET_CHAT = "a";
const IS_THERE_A_CHAT = "b";
const NO_CHAT = "c";
const CREATE_CHAT = "d";
const GET_MESSAGES = "e";
const ALREADY_LOGGED_IN = "f";
const SEND_MESSAGE = "g";
const ADD_USER_TO_CHAT = "h";

const toastDiv = document.getElementById("toast_div");
const toastMessage = document.getElementById("toast_message");
// ************************ JS Starts ************************
responsiveSidebar();
connectToServer();
loginListener();
registerListener();
createChatPopupListener();
sendMessageListener();
fileListener();
emojiListener();
addUserPopupListener();

// ******************** Declare Functions ********************

function responsiveSidebar() {
	document.getElementById("chats_button").onclick = () => {
		document.getElementById("chats_tab").style.display = "flex";
		document.getElementById("persons_tab").style.display = "none";
		document.getElementById("login_area_tab").style.display = "none";

		send(GET_CHATS, "");
	};

	document.getElementById("persons_button").onclick = () => {
		document.getElementById("persons_tab").style.display = "flex";
		document.getElementById("chats_tab").style.display = "none";
		document.getElementById("login_area_tab").style.display = "none";

		send(GET_USERS, "");
	};

	document.getElementById("login_register_button").onclick = () => {
		document.getElementById("login_area_tab").style.display = "flex";
		document.getElementById("persons_tab").style.display = "none";
		document.getElementById("chats_tab").style.display = "none";
	};
}

function loginListener() {
	let loginButton = document.getElementById("login_button");
	let username = document.getElementById("name_area");
	let password = document.getElementById("password_area");

	loginButton.onclick = () => send(LOGIN, username.value + "," + password.value + MESSAGE_END);
}

function registerListener() {
	let registerButton = document.getElementById("register_button");
	let username = document.getElementById("name_area");
	let password = document.getElementById("password_area");

	registerButton.onclick = () => send(REGISTER, username.value + "," + password.value + MESSAGE_END);
}

function emojiListener() {
	let openEmojiAreaButton = document.getElementById("emoji_button");
	let emojiContainer = document.getElementById("emoji_container");

	window.addEventListener("click", (event) => {
		if (!emojiContainer.contains(event.target) && !openEmojiAreaButton.contains(event.target)) {
			emojiContainer.classList.add("hide_popup");
		}
	});

	openEmojiAreaButton.addEventListener("click", () => {
		emojiContainer.classList.remove("hide_popup");
	});

	let emojiElements = document.querySelectorAll(".emoji");
	let messageInput = document.getElementById("message_input");

	emojiElements.forEach((emojiElement) => {
		emojiElement.addEventListener("click", (event) => {
			console.log("girdi");
			console.log(event.target.innerHTML);
			messageInput.value += event.target.innerHTML;
		});
	});
}

function sendMessageListener() {
	let button = document.getElementById("message_send_button");
	let messageArea = document.getElementById("message_input");

	button.onclick = () => sendMessageHandler();

	messageArea.addEventListener("keydown", (event) => {
		if (event.key === "Enter") {
			sendMessageHandler();
		}
	});

	function sendMessageHandler() {
		if (messageArea.value === "") {
			return;
		}

		send(SEND_MESSAGE, CURR_CHAT + "," + TYPE_TEXT + "," + messageArea.value);
		send(GET_MESSAGES, CURR_CHAT);

		messageArea.value = "";
	}
}

function createChatPopupListener() {
	const createChatDiv = document.getElementById("create_chat_div");

	window.addEventListener("click", (event) => {
		if (!createChatDiv.contains(event.target)) {
			createChatDiv.classList.add("hide_chat");
		}
	});
}

function addUserPopupListener() {
	const addUserDiv = document.getElementById("add_user_div");
	const addUserButton = document.getElementById("add_user_button");
	const addUserArea = document.getElementById("add_user_area");
	const openAddUserButton = document.getElementById("open_add_user_div_button");

	window.addEventListener("click", (event) => {
		if (!addUserDiv.contains(event.target) && !openAddUserButton.contains(event.target)) {
			addUserDiv.classList.add("hide_chat");
		}
	});

	openAddUserButton.addEventListener("click", () => {
		addUserDiv.classList.remove("hide_chat");
	});

	addUserButton.addEventListener("click", () => {
		if (addUserArea.value !== "" && addUserArea.value !== null) {
			send(ADD_USER_TO_CHAT, CURR_CHAT + "," + addUserArea.value);
		}
	});
}

function fileListener() {
	let openFileTabButton = document.getElementById("attach_file");
	let dropZoneDiv = document.getElementById("file_drop_zone");
	let fileInput = document.getElementById("file_input");

	window.addEventListener("click", (event) => {
		if (!dropZoneDiv.contains(event.target) && !openFileTabButton.contains(event.target)) {
			dropZoneDiv.classList.add("hide_popup");
		}
	});

	openFileTabButton.addEventListener("click", () => {
		dropZoneDiv.classList.remove("hide_popup");
	});

	fileInput.addEventListener("change", () => {
		let selectedFile = fileInput.files[0];

		if (!selectedFile) {
			return;
		}

		// Check file size
		if (selectedFile.size / 1024 > 60) {
			showMessage(TYPE_WARNING, "File size cannot exceed 60KB.");
			return;
		}

		// Create file stream
		let fileStream = fs.createReadStream(selectedFile.path, { encoding: "base64" });
		let fileData = [];

		fileStream.on("data", (data) => {
			fileData.push(data);
		});

		fileStream.on("end", () => {
			console.log(fileData);
			send(SEND_MESSAGE, CURR_CHAT + "," + TYPE_FILE + "," + getFileTypeAsInt(selectedFile.type) + "," + selectedFile.name + "," + fileData);
		});

		fileStream.on("error", (err) => {
			showMessage(TYPE_ERROR, "Error reading file: " + err.name);
			fileStream.close();
		});
	});
}

function connectToServer() {
	client.connect(PORT, IP_ADDRESS);

	client.on("data", (data) => messageHandler(data));

	client.on("close", () => showMessage(TYPE_ERROR, "Connection closed!"));

	client.on("connect", () => showMessage(TYPE_INFO, "Connected to server."));

	client.on("error", () => showMessage(TYPE_ERROR, "An error occurred in the connection to the server."));
}

function messageHandler(data) {
	let messages = (data + "").split(MESSAGE_END);

	messages.forEach((msg) => {
		let identifier = msg[0];
		let message = msg.substring(1);

		console.log(identifier, message);

		switch (identifier) {
			case SUCCESSFUL_LOGIN:
				showMessage(TYPE_INFO, "User successfully logged in.");
				USER_NAME = message;
				document.getElementById("chats_button").click();
				break;
			case SUCCESSFUL_REGISTER:
				showMessage(TYPE_INFO, "User successfully registered.");
				break;
			case ALREADY_EXIST:
				showMessage(TYPE_WARNING, "This username already exists. Pick another one.");
				break;
			case DOESNT_EXIST:
				showMessage(TYPE_WARNING, "This username doesn't exists. Make sure to register.");
				break;
			case WRONG_PASSWORD:
				showMessage(TYPE_ERROR, "You entered the wrong password.");
				break;
			case PERMISSION_DENIED:
				showMessage(TYPE_ERROR, "You don't have a permission. Make sure to register.");
				break;
			case ALREADY_LOGGED_IN:
				showMessage(TYPE_ERROR, "This user is already logged in.");
				break;
			case GET_USERS:
				let users = JSON.parse(message);
				let usersDiv = document.getElementById("persons_tab");
				usersDiv.innerHTML = "";

				users.forEach((user) => {
					let userDiv = document.createElement("div");
					userDiv.className = "person";
					usersDiv.appendChild(userDiv);

					let userImg = document.createElement("img");
					userImg.className = "person_image";
					userImg.src = "https://robohash.org/" + user.imageHash;
					userDiv.appendChild(userImg);

					let userName = document.createElement("h1");
					userName.className = "person_name";
					userName.innerHTML = user.username;
					userDiv.appendChild(userName);

					userDiv.onclick = () => send(IS_THERE_A_CHAT, user.username);
				});
				break;
			case GET_CHATS:
				let chats = JSON.parse(message);
				let chatsDiv = document.getElementById("chats_tab");
				if (chats.length === 0) {
					break;
				}

				chatsDiv.innerHTML = "";

				chats.forEach((chat) => {
					let chatDiv = document.createElement("div");
					chatDiv.className = "chat";
					chatDiv.id = chat.id;
					chatsDiv.appendChild(chatDiv);

					let chatImage = document.createElement("div");
					chatImage.className = "chat_image";
					chatImage.style.backgroundColor = getColorAsHex(chat.color);
					chatDiv.appendChild(chatImage);

					let infoDiv = document.createElement("div");
					infoDiv.className = "infos";
					chatDiv.appendChild(infoDiv);

					let chatName = document.createElement("h1");
					chatName.className = "chat_name";
					chatName.innerHTML = chat.chatname;
					infoDiv.appendChild(chatName);

					let chatPersons = document.createElement("h2");
					chatPersons.className = "chat_persons";
					chatPersons.innerHTML = chat.users.map((user) => user.username);
					infoDiv.appendChild(chatPersons);

					chatDiv.onclick = () => {
						CURR_CHAT = chat.id;
						send(GET_CHAT, chat.id);
						send(GET_MESSAGES, chat.id);
					};
				});
				break;
			case NO_CHAT:
				let userName = message;
				let createChatDiv = document.getElementById("create_chat_div");
				let input = document.getElementById("create_chat_area");
				let button = document.getElementById("create_chat_button");

				createChatDiv.classList.remove("hide_chat");
				button.onclick = () => {
					send(CREATE_CHAT, input.value + "," + userName);
					createChatDiv.classList.add("hide_chat");
				};
				break;
			case GET_CHAT:
				// document.getElementById("chats_button").click();
				let chat = JSON.parse(message);
				document.getElementById("chat_area_name").innerHTML = chat.chatname;
				document.getElementById("chat_area_persons").innerHTML = chat.users.map((user) => user.username);
				document.getElementById("chat_area_image").style.backgroundColor = getColorAsHex(chat.color);
				break;
			case GET_MESSAGES:
				let i = message.indexOf(",");
				let infos = [message.slice(0, i), message.slice(i + 1)];

				let chatId = infos[0];
				let messages = JSON.parse(infos[1]);

				let messagesDiv = document.getElementById("messages");
				messagesDiv.innerHTML = "";

				CURR_CHAT = chatId;

				messages.forEach((msg) => {
					let bubbleClass = USER_NAME === msg.owner ? "right_message_bubble" : "left_message_bubble";
					let bubbleDiv = document.createElement("div");
					bubbleDiv.className = bubbleClass;
					messagesDiv.appendChild(bubbleDiv);

					let ownerH1 = document.createElement("h1");
					ownerH1.innerHTML = msg.owner;
					bubbleDiv.appendChild(ownerH1);

					let messageDiv = document.createElement("div");
					messageDiv.className = "message";
					bubbleDiv.appendChild(messageDiv);

					let messageParagraph;

					if (msg.type === TYPE_FILE) {
						messageParagraph = document.createElement("a");

						const decodedText = Buffer.from(msg.message, "base64"); // atob(fileString); deprecated
						const dataURL = `data:${msg.file_format};base64,${decodedText}`;
						messageParagraph.innerHTML = msg.file_name;
						messageParagraph.href = dataURL;
						messageParagraph.download = msg.file_name;
					} else {
						messageParagraph = document.createElement("p");
						messageParagraph.innerHTML = msg.message;
					}

					messageDiv.appendChild(messageParagraph);
				});

				messagesDiv.scroll({ top: messagesDiv.scrollHeight, behavior: "smooth" });
				break;
		}
	});
}

function send(identifier, message) {
	client.write(identifier + message + MESSAGE_END);
}

function showMessage(type, message) {
	switch (type) {
		case 0:
			toastDiv.style.borderColor = "green";
			toastDiv.style.backgroundColor = "rgba(0, 255, 0, 0.5)";
			break;
		case 1:
			toastDiv.style.borderColor = "orange";
			toastDiv.style.backgroundColor = "rgba(255, 127, 0, 0.5)";
			break;
		case 2:
			toastDiv.style.borderColor = "red";
			toastDiv.style.backgroundColor = "rgba(255, 0, 0, 0.5)";
			break;
	}

	toastMessage.innerHTML = message;
	toastDiv.classList.remove("hide_toast");
	setTimeout(() => toastDiv.classList.add("hide_toast"), 4000);
}

function getColorAsHex(color) {
	return `#${color.toString(16).padStart(6, "0")}`;
}

function getFileTypeAsInt(fileType) {
	return fileTypesMap[fileType] || -1; // Return -1 if the file type is not found in the map
}
