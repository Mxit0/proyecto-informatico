import { io } from "socket.io-client";
const TOKEN_VENDEDOR = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZF91c3VhcmlvIjoxNSwibm9tYnJlX3VzdWFyaW8iOiJWZW5kZWRvciIsImNvcnJlbyI6InZlbmRlZG9yQHRlc3QuY29tIiwiZm90byI6bnVsbCwicmVwdXRhY2lvbiI6MCwiaWF0IjoxNzY0MjA3OTM5LCJleHAiOjE3NjQ4MTI3Mzl9.erRKIoJaPPrAUfaofk-SMFcetRt0I2mbtLUM7yEwhN0";
const CHAT_ID = 5; // sale cuando el comprador abre chat
const socket = io("http://localhost:3000", {
  auth: { token: TOKEN_VENDEDOR },
});

socket.on("connect", () => {
  console.log("✅ Vendedor conectado");

  socket.emit("join_chat", { chatId: CHAT_ID });
});

socket.on("new_message", (msg) => {
  console.log("📩 (Vendedor) nuevo mensaje:", msg);
});

socket.on("disconnect", () => {
  console.log("Vendedor desconectado");
});