// routes/userRoutes.js
import express from "express";
import {
  getAllUsers,
  getUserById,
  updateReputation,
  updatePhoto,
} from "../controllers/userController.js";

import { uploadSingleImage } from "../middleware/uploadMiddleware.js";
import { requireAuth } from "../middleware/auth.js";   
import { saveFcmToken } from "../controllers/fcmController.js"; 

const router = express.Router();

router.get("/", getAllUsers);
router.get("/:id", getUserById);
router.patch("/:id/reputacion", updateReputation);


router.patch("/:id/foto", uploadSingleImage, updatePhoto);


router.post("/fcm-token", requireAuth, saveFcmToken);

export default router;
