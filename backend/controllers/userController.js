// controllers/userController.js
import {
  listUsersService,
  getUserByIdService,
  updateReputationService,
   updatePhotoService, 
} from '../services/userService.js';

export const getAllUsers = async (_req, res) => {
  try {
    const usuarios = await listUsersService();
    res.json({ ok: true, usuarios });
  } catch (error) {
    res.status(500).json({ ok: false, message: error.message });
  }
};

export const getUserById = async (req, res) => {
  try {
    const usuario = await getUserByIdService(req.params.id);
    if (!usuario) {
      return res.status(404).json({ ok: false, message: 'Usuario no encontrado' });
    }
    res.json({ ok: true, usuario });
  } catch (error) {
    res.status(500).json({ ok: false, message: error.message });
  }
};

export const updateReputation = async (req, res) => {
  try {
    const { reputacion } = req.body;
    if (reputacion === undefined) {
      return res.status(400).json({ ok: false, message: 'Reputación requerida' });
    }

    const usuario = await updateReputationService(req.params.id, reputacion);
    res.json({ ok: true, usuario });
  } catch (error) {
    res.status(500).json({ ok: false, message: error.message });
  }
};
export const updatePhoto = async (req, res) => {
  try {
    const { id } = req.params;

    if (!req.file) {
      return res
        .status(400)
        .json({ ok: false, message: "No se envió ningún archivo 'foto'" });
    }

    const usuario = await updatePhotoService(id, req.file);

    res.json({ ok: true, usuario });
  } catch (error) {
    console.error("Error en updatePhoto:", error);
    res.status(500).json({ ok: false, message: error.message });
  }
};
