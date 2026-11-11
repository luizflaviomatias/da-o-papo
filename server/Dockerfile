# Servidor do Dá o Papo. Construa a partir da raiz do repositório:
#   docker build -t da-o-papo-servidor -f server/Dockerfile .
FROM node:22-alpine
WORKDIR /app
COPY agent ./agent
COPY server ./server
ENV NODE_ENV=production PORT=8787
EXPOSE 8787
USER node
CMD ["node", "server/src/index.mjs"]
