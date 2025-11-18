const express = require('express');
const router = express.Router();
const compatibilityController = require('../controllers/compatibilityController');
const authMiddleware = require('../middleware/auth');

router.post('/check', authMiddleware, compatibilityController.checkCompatibility);
router.get('/history', authMiddleware, compatibilityController.getCompatibilityHistory);

module.exports = router;