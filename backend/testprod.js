async function testGet() {
  console.log("Enviando petición GET a http://localhost:3000/productos ...");
  
  try {
    const response = await fetch('http://localhost:3000/productos');
    const data = await response.json();

    if (!response.ok) {
      console.error('¡Error! El servidor respondió:', data);
    } else {
      console.log('¡Éxito! Se obtuvieron los productos:');
      console.log(data); // Debería mostrar '[]' (un array vacío)
    }

  } catch (error) {
    console.error('Error de conexión o de parseo de respuesta:', error.message);
  }
}
// Archivo: testprod.js

// 1. Define el objeto JavaScript (¡limpio, sin comillas en las llaves!)
const productoNuevo = {
  nombre: "It´s over nine THOUSAND!!!",
  precio: 9999,
  descripcion: "Vegeta wekito",
  id_usuario: 4, // <-- ¡IMPORTANTE! Asegúrate que este ID exista en tu tabla 'usuario'
  stock: 10,
  categoria: "Figuras de acción"
};

async function testInsert() {
  console.log("Enviando petición POST a http://localhost:3000/productos ...");
  
  try {
    const response = await fetch('http://localhost:3000/productos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      // 2. JSON.stringify convierte el objeto en un string de JSON VÁLIDO
      body: JSON.stringify(productoNuevo) 
    });

    // Intentamos leer la respuesta como JSON
    const data = await response.json();

    if (!response.ok) {
      // Si la respuesta es un 400 o 500 (error)
      console.error('¡Error! El servidor respondió:', data);
    } else {
      // Si la respuesta es 201 (Creado)
      console.log('¡ÉXITO! Producto creado:');
      console.log(data);
    }

  } catch (error) {
    // Este error salta si la conexión falla o si la respuesta NO es JSON
    console.error('Error de conexión o de parseo de respuesta:', error.message);
  }
}

// Ejecutar la función
//testInsert();
testGet();