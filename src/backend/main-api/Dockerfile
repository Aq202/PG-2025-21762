FROM node:22-alpine

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

RUN cp -r ./config ./dist/ 2>/dev/null || echo "No config directory found"

EXPOSE 3000

CMD ["npm", "start"]