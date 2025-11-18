import productRepository2 from '../repositories/productRepository2.js';
import { supabase } from '../lib/supabaseClient.js';

// ID de usuario de prueba (debes usar uno real de tu base de datos)
// Puedes obtenerlo de la tabla auth.users en Supabase
const USER_ID = '1'; // REEMPLAZA ESTO

const sampleProducts = [
  {
    nombre: "Intel Core i7-12700K",
    descripcion: "Procesador Intel Core i7 12ma generación, excelente estado",
    precio: 350.00,
    id_usuario: USER_ID,
    stock: 5,
    categoria: "procesador",
    especificaciones: {
      socket: "LGA 1700",
      nucleos: 12,
      hilos: 20,
      frecuencia_base: "3.6 GHz",
      frecuencia_turbo: "5.0 GHz",
      tdp: "125W",
      generacion: "12th Gen",
      memoria_compatible: "DDR5, DDR4",
      graficos_integrados: "Intel UHD Graphics 770"
    }
  },
  {
    nombre: "ASUS ROG STRIX Z690-F GAMING",
    descripcion: "Placa madre ASUS ROG Z690, casi nueva con garantía",
    precio: 450.00,
    id_usuario: USER_ID,
    stock: 3,
    categoria: "placa_madre",
    especificaciones: {
      socket: "LGA 1700",
      chipset: "Z690",
      formato: "ATX",
      memoria_soportada: "DDR5",
      slots_ram: 4,
      max_ram: "128GB",
      slots_pcie: "PCIe 5.0 x16, PCIe 4.0 x16",
      almacenamiento: "4x M.2, 6x SATA III",
      conectividad: "WiFi 6E, 2.5G Ethernet"
    }
  },
  {
    nombre: "Corsair Vengeance RGB DDR5 32GB (2x16GB)",
    descripcion: "Kit de memoria DDR5 5600MHz con iluminación RGB",
    precio: 180.00,
    id_usuario: USER_ID,
    stock: 10,
    categoria: "memoria_ram",
    especificaciones: {
      tipo: "DDR5",
      capacidad: "32GB",
      velocidad: "5600MHz",
      latencia: "CL36",
      voltaje: "1.25V",
      formato: "DIMM",
      configuracion: "2x16GB",
      disipador: "RGB"
    }
  },
  {
    nombre: "AMD Ryzen 7 5800X",
    descripcion: "Procesador AMD Ryzen 7, 8 núcleos, usado 6 meses",
    precio: 280.00,
    id_usuario: USER_ID,
    stock: 4,
    categoria: "procesador",
    especificaciones: {
      socket: "AM4",
      nucleos: 8,
      hilos: 16,
      frecuencia_base: "3.8 GHz",
      frecuencia_turbo: "4.7 GHz",
      tdp: "105W",
      generacion: "5000 Series",
      memoria_compatible: "DDR4",
      graficos_integrados: "No"
    }
  },
  {
    nombre: "MSI B550 TOMAHAWK",
    descripcion: "Placa madre MSI B550, perfecto estado",
    precio: 220.00,
    id_usuario: USER_ID,
    stock: 2,
    categoria: "placa_madre",
    especificaciones: {
      socket: "AM4",
      chipset: "B550",
      formato: "ATX",
      memoria_soportada: "DDR4",
      slots_ram: 4,
      max_ram: "128GB",
      slots_pcie: "PCIe 4.0 x16",
      almacenamiento: "2x M.2, 6x SATA III"
    }
  },
  {
    nombre: "NVIDIA GeForce RTX 4070 Ti",
    descripcion: "Tarjeta gráfica NVIDIA RTX 4070 Ti, usada para mining 3 meses",
    precio: 750.00,
    id_usuario: USER_ID,
    stock: 1,
    categoria: "tarjeta_grafica",
    especificaciones: {
      gpu: "NVIDIA RTX 4070 Ti",
      vram: "12GB GDDR6X",
      interfaz: "PCIe 4.0 x16",
      conectores: "3x DisplayPort, 1x HDMI",
      alimentacion: "16-pin PCIe",
      consumo: "285W",
      longitud: "336mm"
    }
  }
];

async function seedProducts() {
  try {
    console.log('Iniciando inserción de productos de prueba...');
    
    for (const product of sampleProducts) {
      await productRepository2.createProduct(product);
      console.log(`✅ Producto creado: ${product.nombre}`);
    }
    
    console.log('🎉 Todos los productos de prueba han sido creados exitosamente');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error creando productos:', error);
    process.exit(1);
  }
}

// Ejecutar solo si se llama directamente
if (import.meta.url === `file://${process.argv[1]}`) {
  seedProducts();
}

export default seedProducts;