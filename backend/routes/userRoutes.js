// routes/userRoutes.js
import express from "express";
import {
  getAllUsers,
  getUserById,
  updateReputation,
  updatePhoto,           // 👈 import nuevo
} from "../controllers/userController.js";

import { uploadSingleImage } from "../middleware/uploadMiddleware.js";

const router = express.Router();

router.get("/", getAllUsers);
router.get("/:id", getUserById);
router.patch("/:id/reputacion", updateReputation);

// ⭐ NUEVA RUTA PARA SUBIR FOTO
router.patch("/:id/foto", uploadSingleImage, updatePhoto);

export default router;
