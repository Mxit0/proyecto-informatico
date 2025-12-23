// testSendMessage.js
const { io } = require("socket.io-client");

const TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZF91c3VhcmlvIjoxLCJub21icmVfdXN1YXJpbyI6IkFsZXgiLCJjb3JyZW8iOiJhbGV4QGV4YW1wbGUuY29tIiwiZm90byI6Imh0dHBzOi8vbnZvY3phd3dwYW93c3Fqem1jYnIuc3VwYWJhc2UuY28vc3RvcmFnZS92MS9vYmplY3QvcHVibGljL3VzdWFyaW9zL3VzdWFyaW9fMS8xNzYzNjAzNzE5NjkxLmpwZyIsInJlcHV0YWNpb24iOjAsImlhdCI6MTc2NTE1NDI1OSwiZXhwIjoxNzY1NzU5MDU5fQ.RB7cmVqx9jsSkAnqvcQQiXIEIJIs7zrTMRoiQ8QjqZo";

const socket = io("http://localhost:3000", {
  auth: {
    token: `Bearer ${TOKEN}`,
  },
});

socket.on("connect", () => {
  console.log("Conectado al socket:", socket.id);

  const chatId = 2; 

  socket.emit(
    "send_message",
    { chatId, contenido: "Mensaje de prueba FCM desde script" },
    (resp) => {
      console.log("Respuesta send_message:", resp);
      socket.disconnect();
    }
  );
});

socket.on("connect_error", (err) => {
  console.error("Error de conexión:", err.message);
});

socket.on("disconnect", () => {
  console.log("Socket desconectado");
});
