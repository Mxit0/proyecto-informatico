import { io } from "socket.io-client";
const TOKEN_COMPRADOR = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZF91c3VhcmlvIjoxNSwibm9tYnJlX3VzdWFyaW8iOiJWZW5kZWRvciIsImNvcnJlbyI6InZlbmRlZG9yQHRlc3QuY29tIiwiZm90byI6bnVsbCwicmVwdXRhY2lvbiI6MCwiaWF0IjoxNzY0MjA3OTM5LCJleHAiOjE3NjQ4MTI3Mzl9.erRKIoJaPPrAUfaofk-SMFcetRt0I2mbtLUM7yEwhN0";
const ID_VENDEDOR = 1;
const socket = io("http://localhost:3000", {
  auth: { token: TOKEN_COMPRADOR },
});

socket.on("connect", () => {
  console.log("✅ Comprador conectado");

  socket.emit(
    "open_chat_with_user",
    { otherUserId: ID_VENDEDOR },
    (resp) => {
      console.log("Respuesta open_chat:", resp);
      if (!resp || !resp.ok) return;

      const chatId = resp.chat.id;
      console.log("Chat ID:", chatId);

      socket.emit("join_chat", { chatId });

      setTimeout(() => {
        socket.emit(
          "send_message",
          { chatId, contenido: "Hola, ¿sigue disponible?" },
          (r) => console.log("Respuesta send_message:", r)
        );
      }, 2000);
    }
  );
});

socket.on("new_message", (msg) => {
  console.log("📩 (Comprador) nuevo mensaje:", msg);
});

socket.on("disconnect", () => {
  console.log("Comprador desconectado");
});