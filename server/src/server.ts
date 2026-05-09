import 'dotenv/config';
import Fastify from 'fastify';
import cors from '@fastify/cors';
import helmet from '@fastify/helmet';
import { registerAuthRoutes } from './routes/auth.js';
import { registerFlightRoutes } from './routes/flights.js';
import { registerCheckInRoutes } from './routes/checkin.js';
import { registerUserRoutes } from './routes/user.js';
import { registerSyncRoutes } from './routes/sync.js';
import { prisma } from './lib/prisma.js';

const PORT = Number(process.env.PORT) || 8082;
const HOST = process.env.HOST ?? '0.0.0.0';

const app = Fastify({
  logger: {
    transport: {
      target: 'pino-pretty',
      options: { colorize: true, translateTime: 'HH:MM:ss', ignore: 'pid,hostname' },
    },
  },
});

await app.register(helmet, { contentSecurityPolicy: false });
await app.register(cors, { origin: true, credentials: true });

// ─────────────── Health check ───────────────
app.get('/api/health', async () => ({
  status: 'ok',
  service: 'MyPass Backend',
  version: '2.0.0',
  runtime: 'Node.js',
  timestamp: new Date().toISOString(),
}));

// ─────────────── Register routes ───────────────
await registerAuthRoutes(app);
await registerFlightRoutes(app);
await registerCheckInRoutes(app);
await registerUserRoutes(app);
await registerSyncRoutes(app);

// ─────────────── Global error handler ───────────────
app.setErrorHandler((err, _req, reply) => {
  app.log.error(err);
  const e = err as { statusCode?: number; message?: string };
  reply.status(e.statusCode ?? 500).send({
    success: false,
    message: e.message ?? 'Internal server error',
  });
});

// ─────────────── Graceful shutdown ───────────────
const shutdown = async () => {
  app.log.info('Shutting down gracefully...');
  await app.close();
  await prisma.$disconnect();
  process.exit(0);
};
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);

// ─────────────── Start ───────────────
try {
  await prisma.$connect();
  app.log.info('✓ PostgreSQL connection established.');

  await app.listen({ port: PORT, host: HOST });
  app.log.info(`╔════════════════════════════════════════════════════════════╗`);
  app.log.info(`║  MyPass Backend ready on http://${HOST}:${PORT}              `);
  app.log.info(`║  Local:   http://localhost:${PORT}/api/health              `);
  app.log.info(`║  Network: http://<your-LAN-IP>:${PORT}/api/health          `);
  app.log.info(`╚════════════════════════════════════════════════════════════╝`);
} catch (err) {
  app.log.error(err, 'Failed to start server');
  process.exit(1);
}
