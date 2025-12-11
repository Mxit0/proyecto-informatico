import express from 'express';
import compatibilityController from '../controllers/compatibilityController.js';
import authMiddleware from '../middleware/auth.js';

const router = express.Router();

router.post('/check', authMiddleware, compatibilityController.checkCompatibility);
router.get('/history', authMiddleware, compatibilityController.getCompatibilityHistory);

export default router;