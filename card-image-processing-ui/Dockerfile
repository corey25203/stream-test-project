FROM node:21.7.0-bookworm
EXPOSE 8087 3000
ENV PORT = 8087
ENV FILE_UPLOAD_URL http://localhost:8080/uploadFile
ENV FILES_LIST_URL http://localhost:8080/files
ENV FILE_PARSE_URL http://localhost:8080/parseFile
ENV TOPIC_RESPONSE_URL ws://localhost:8080/framedesc

WORKDIR /app
COPY package.json /app
RUN npm install

COPY ./src /app/src
COPY ./public /app/public

CMD [ "npm", "start" ]