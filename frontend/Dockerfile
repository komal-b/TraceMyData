# Use the latest LTS version of Node.js
FROM node:18-alpine

# Set the working directory inside the container
WORKDIR /app
 
# Copy package.json and package-lock.json
COPY package.json ./
 
# Install dependencies
RUN npm install
RUN npm rebuild esbuild
RUN npm install react-toastify
RUN npm install react-router-dom @types/react-router-dom
RUN npm install react @types/react
RUN npm install npm install react react-dom
RUN npm install jwt-decode
RUN npm install @react-oauth/google
RUN npm install rc-progress

# Copy the rest of your application files
COPY . .
 
# Expose the port your app runs on
EXPOSE 5173
 
# Define the command to run your app
CMD ["npm", "run", "dev", "--", "--host"]

ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID
 

