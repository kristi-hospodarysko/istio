FROM node:14.4.0-alpine

WORKDIR /app

COPY package*.json /app/
RUN npm install
COPY . /app/

EXPOSE 3000

CMD node bin/www
