# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Host using Nginx
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Copy custom Nginx configuration to support SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
