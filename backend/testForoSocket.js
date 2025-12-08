// backend/testForoSocket.js
import { io } from "socket.io-client";

const TOKEN =
  "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZF91c3VhcmlvIjoxOCwibm9tYnJlX3VzdWFyaW8iOiJKb2FxdWluIiwiY29ycmVvIjoiam9hcXVpbkB0ZXN0LmNvbSIsImZvdG8iOm51bGwsInJlcHV0YWNpb24iOjAsImlhdCI6MTc2NTE1OTk4NywiZXhwIjoxNzY1NzY0Nzg3fQ.dnq7214VUjuX_luPLOTFsFT83L3O5oHc0PxNz2LC7gY";

const FORO_ID = 1; // cambia al foro que quieras probar

const socket = io("http://localhost:3000", {
  auth: { token: TOKEN },
});

socket.on("connect", () => {
  console.log("✅ Conectado al Socket.IO, id:", socket.id);

  // nos unimos a la sala del foro
  socket.emit("join_forum", { foroId: FORO_ID });
  console.log("📌 join_forum enviado para foro", FORO_ID);
});

// Escuchar nuevas publicaciones del foro
socket.on("new_forum_post", (post) => {
  console.log("🔥 Nueva publicación recibida en foro:", post);
});

socket.on("connect_error", (err) => {
  console.error("❌ Error de conexión:", err.message);
});

socket.on("disconnect", () => {
  console.log("🔌 Desconectado del socket");
});