import { supabase } from '../lib/supabaseClient.js';

// Función para obtener un ID de usuario real de tu tabla "usuario"
async function getRealUserId() {
  try {
    console.log('🔍 Buscando usuarios en la tabla "usuario"...');
    
    const { data, error } = await supabase
      .from('usuario')
      .select('id_usuario')
      .limit(1);

    if (error) {
      console.error('❌ Error obteniendo usuarios:', error);
      return null;
    }

    if (!data || data.length === 0) {
      console.log('⚠️ No hay usuarios en la tabla "usuario"');
      console.log('💡 Crea un usuario primero mediante el registro en tu app');
      return null;
    }

    const userId = data[0].id_usuario;
    console.log(`✅ ID_USUARIO obtenido: ${userId}`);
    return userId;

  } catch (error) {
    console.error('❌ Error inesperado:', error);
    return null;
  }
}

const sampleProducts = [
  {
    nombre: "Intel Core i7-12700K",
    descripcion: "Procesador Intel Core i7 12ma generación, excelente estado",
    precio: 350.00,
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
    descripcion: "Tarjeta gráfica NVIDIA RTX 4070 Ti, excelente estado",
    precio: 750.00,
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
  },
  {
    nombre: "EVGA 750W 80+ Gold",
    descripcion: "Fuente de poder EVGA 750W certificación Gold",
    precio: 120.00,
    stock: 3,
    categoria: "fuente_poder",
    especificaciones: {
      potencia: "750W",
      certificacion: "80+ Gold",
      modular: "Semi-modular",
      conectores_pcie: "4x 8-pin",
      conectores_sata: "8x SATA",
      eficiencia: "90%"
    }
  }
];

async function createProductDirect(productData, userId) {
  const productWithUser = {
    ...productData,
    id_usuario: userId
  };

  const { data, error } = await supabase
    .from('producto_compatibilidad')
    .insert([productWithUser])
    .select()
    .single();

  if (error) {
    console.error(`❌ Error creando producto ${productData.nombre}:`, error);
    throw error;
  }
  return data;
}

async function seedProducts(userId) {
  try {
    console.log('📦 Iniciando inserción de productos de prueba...');
    
    // Verificar que la tabla producto_compatibilidad existe
    const { data: tableCheck, error: tableError } = await supabase
      .from('producto_compatibilidad')
      .select('id')
      .limit(1);
    
    if (tableError) {
      console.error('❌ Error accediendo a la tabla producto_compatibilidad:');
      console.error('💡 Asegúrate de que la tabla existe en Supabase');
      console.error('💡 Ejecuta este SQL primero en el editor de Supabase:');
      console.log(`
        CREATE TABLE producto_compatibilidad (
          id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
          nombre VARCHAR(255) NOT NULL,
          descripcion TEXT,
          precio DECIMAL(10,2),
          id_usuario BIGINT NOT NULL,
          stock INTEGER DEFAULT 1,
          categoria VARCHAR(100) NOT NULL,
          fecha_publicacion TIMESTAMP DEFAULT NOW(),
          especificaciones JSONB NOT NULL DEFAULT '{}',
          created_at TIMESTAMP DEFAULT NOW(),
          updated_at TIMESTAMP DEFAULT NOW()
        );
      `);
      return;
    }
    
    console.log('✅ Tabla producto_compatibilidad encontrada');
    
    let successCount = 0;
    let errorCount = 0;
    
    for (const product of sampleProducts) {
      try {
        const createdProduct = await createProductDirect(product, userId);
        console.log(`✅ Producto creado: ${product.nombre} (ID: ${createdProduct.id})`);
        successCount++;
      } catch (error) {
        console.log(`❌ Error con ${product.nombre}:`, error.message);
        errorCount++;
      }
      
      // Pequeña pausa para no saturar
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    console.log('\n🎉 Resumen de la inserción:');
    console.log(`✅ Productos creados exitosamente: ${successCount}`);
    console.log(`❌ Errores: ${errorCount}`);
    console.log(`📊 Total procesado: ${sampleProducts.length}`);
    
  } catch (error) {
    console.error('❌ Error inesperado en seedProducts:', error);
  }
}

// Función principal
async function main() {
  console.log('🚀 Iniciando script de inserción de productos...\n');
  
  const userId = await getRealUserId();
  
  if (!userId) {
    console.log('\n💡 Soluciones posibles:');
    console.log('1. Crea un usuario manualmente en tu app');
    console.log('2. O usa este comando SQL en Supabase para obtener un ID:');
    console.log('   SELECT id_usuario FROM usuario LIMIT 1;');
    console.log('3. Si no hay usuarios, crea uno con este SQL:');
    console.log(`
      INSERT INTO usuario (nombre_usuario, correo, password) 
      VALUES ('usuario_prueba', 'test@example.com', 'password123');
    `);
    return;
  }
  
  await seedProducts(userId);
  
  console.log('\n✨ Script completado');
}

// Ejecutar el script
main().catch(console.error);