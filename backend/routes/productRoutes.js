import express from "express";
import multer from "multer";
import {
  getAllProducts,
  getProductById,
  createProduct,
  uploadProductImages,
  getProductImages,
  updateProduct,
  getAllCategories,
  getProductsByCategory,
} from "../repositories/productRepository.js";
import { getComponentsByCategory } from "../repositories/componenteRepository.js";

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

router.get("/", async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100;
    const products = await getAllProducts(page, limit);
    res.json(products);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener productos: " + error.message });
  }
});

// Ruta alternativa más simple para obtener categorías
router.get("/categorias", async (req, res) => {
  try {
    const categories = await getAllCategories();
    res.json(categories);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// 1. Obtener todas las categorías
router.get("/categorias/todas", async (req, res) => {
  try {
    const categories = await getAllCategories();
    res.json(categories);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Obtener componentes maestros por categoría
router.get("/componentes/categoria/:categoryId", async (req, res) => {
  try {
    const categoryId = parseInt(req.params.categoryId);
    const componentes = await getComponentsByCategory(categoryId);
    res.json(componentes);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// 2. Obtener productos por categoría
router.get("/categoria/:categoryId", async (req, res) => {
  try {
    const categoryId = parseInt(req.params.categoryId);
    const products = await getProductsByCategory(categoryId);
    res.json(products);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    const product = await getProductById(id);
    if (!product) {
      return res.status(404).json({ error: "Producto no encontrado" });
    }
    res.json(product);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener el producto: " + error.message });
  }
});

router.post("/", async (req, res) => {
  try {
    const newProductData = req.body;
    const {
      nombre,
      precio,
      descripcion,
      id_usuario,
      stock,
      categoria,
      id_componente_maestro,
    } = newProductData;

    if (
      !nombre ||
      !precio ||
      !descripcion ||
      !id_usuario ||
      !stock ||
      !categoria ||
      !id_componente_maestro
    ) {
      return res.status(400).json({
        error:
          "Datos incompletos. Se requieren: nombre, precio, descripcion, id_usuario, stock, categoria, id_componente_maestro.",
      });
    }
    newProductData.fecha_publicacion = new Date().toISOString();

    const createdProduct = await createProduct(newProductData);
    res.status(201).json(createdProduct);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al crear el producto: " + error.message });
  }
});

router.post("/:id/imagenes", upload.array("imagenes", 10), async (req, res) => {
  try {
    const id_producto = req.params.id;
    const files = req.files;

    if (!files || files.length === 0) {
      return res.status(400).json({ error: "No se enviaron archivos." });
    }

    if (files.length < 3) {
      return res.status(400).json({
        error: "Se requieren al menos 3 imágenes para publicar el producto.",
      });
    }

    const urls = await uploadProductImages(id_producto, files);

    res.status(201).json({
      message: "Imágenes subidas exitosamente",
      urls: urls,
    });
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al subir imágenes: " + error.message });
  }
});

router.get("/:id/imagenes", async (req, res) => {
  try {
    const { id } = req.params;
    const images = await getProductImages(id);

    res.json(images || []);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener las imágenes: " + error.message });
  }
});

router.patch("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    const updates = req.body;

    // Validación simple: ¿Enviaron algo para cambiar?
    if (Object.keys(updates).length === 0) {
      return res
        .status(400)
        .json({ error: "No se enviaron datos para actualizar." });
    }

    // Llamamos a la función del repositorio
    const updatedProduct = await updateProduct(id, updates);

    if (!updatedProduct) {
      return res
        .status(404)
        .json({ error: "Producto no encontrado o no se pudo actualizar" });
    }

    res.json(updatedProduct);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al actualizar el producto: " + error.message });
  }
});

export default router;
