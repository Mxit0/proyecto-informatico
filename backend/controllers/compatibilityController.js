import compatibilityService from '../services/compatibilityService.js';

const compatibilityController = {
  async checkCompatibility(req, res) {
    try {
      const { items } = req.body;
      const userId = req.user?.id; // Del middleware de auth

      if (!items || !Array.isArray(items)) {
        return res.status(400).json({
          error: 'Se requiere un array de items en el carrito'
        });
      }

      const result = await compatibilityService.checkCompatibility(items, userId);
      
      res.json({
        success: true,
        data: result
      });
    } catch (error) {
      console.error('Error in compatibility controller:', error);
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  },

  async getCompatibilityHistory(req, res) {
    try {
      const userId = req.user.id;
      // Implementar según tu base de datos
      const history = []; // await compatibilityService.getUserCompatibilityHistory(userId);
      
      res.json({
        success: true,
        data: history
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }
};

export default compatibilityController;