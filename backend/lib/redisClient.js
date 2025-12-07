// backend/lib/redisClient.js
import { createClient } from "redis";

let redisClient = {
  // implementación "dummy" para desarrollo si no hay Redis
  get: async () => null,
  setEx: async () => {},
  del: async () => {},
  keys: async () => [],
};

const redisUrl = process.env.REDIS_URL || ""; // si está vacío, no intentamos conectar

if (redisUrl) {
  const client = createClient({ url: redisUrl });

  client.on("error", (err) => {
    console.log("Redis Client Error (ignorado en dev):", err.message);
  });

  client
    .connect()
    .then(() => {
      console.log("Conectado a Redis:", redisUrl);
      redisClient = client;
    })
    .catch((err) => {
      console.log("No se pudo conectar a Redis, usando cliente dummy:", err.message);
    });
}

export default redisClient;
