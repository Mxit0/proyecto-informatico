// middleware/uploadMiddleware.js
import multer from "multer";

const storage = multer.memoryStorage(); // guarda el archivo en RAM

const upload = multer({ storage });

// vamos a recibir 1 archivo en el campo "foto"
export const uploadSingleImage = upload.single("foto");
