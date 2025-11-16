// routes/userRoutes.js
import express from "express";
import {
  getAllUsers,
  getUserById,
  updateReputation,
} from "../controllers/userController.js";

const router = express.Router();

router.get("/", getAllUsers);
router.get("/:id", getUserById);
router.patch("/:id/reputacion", updateReputation);

export default router;
