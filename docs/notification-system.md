# Notification System Documentation

This document describes the implementation of the notification system using WebSocket for the Clothing Store e-commerce application.

## Overview

The notification system provides real-time notifications to users for various events:

1. **Order Status Updates**: Users receive notifications when their order status changes (e.g., processing, shipping, delivered).
2. **Promotional Notifications**: Scheduled notifications about upcoming promotions, including a list of products on sale.

## Technical Implementation

### Architecture

The system uses:

- **WebSocket** with Spring's STOMP support for real-time messaging
- **MySQL Database** for persistent storage of notifications
- **Scheduled Jobs** for sending promotional notifications

### Core Components

1. **Notification Entity**: Represents a notification stored in the database

   - User-specific notifications
   - Broadcast notifications (promotions)
   - Supports scheduling via `scheduledDate` field

2. **WebSocket Configuration**: Sets up WebSocket endpoints and message brokers

   - User-specific destination: `/user/{userId}/queue/notifications`
   - Broadcast destination: `/topic/promotions`

3. **Notification Service**: Core service handling notification logic

   - Creating notifications
   - Sending notifications via WebSocket
   - Marking notifications as read
   - Processing scheduled notifications

4. **Notification Scheduler**: Scheduled job that runs every minute to process pending notifications

### Client Subscription Guide

Frontend applications should:

1. Connect to the WebSocket endpoint: `/ws`
2. Subscribe to user-specific notifications: `/user/queue/notifications`
3. Subscribe to broadcast notifications: `/topic/promotions`

### Notification Flow

#### Order Status Update Notifications

1. When an order status is updated in `OrderServiceImpl.updateOrderStatus()`:

   - A notification is created with the new status
   - The notification is stored in the database
   - The notification is sent to the user via WebSocket

2. Client receives the notification and can:
   - Display it as an alert/toast
   - Store it in the client's notification list
   - Navigate to order details when clicked

#### Promotion Notifications

1. Admin creates a promotion notification with optional scheduling:

   - If scheduled, it's stored with `scheduledDate` and `sent=false`
   - If not scheduled, it's sent immediately to all users

2. The notification scheduler processes pending notifications:

   - Finds notifications where `scheduledDate <= now` and `sent=false`
   - Sends them to all users via the broadcast topic
   - Marks them as sent

3. Client receives the promotion notification with product list and can:
   - Display it with product images
   - Navigate to product details when clicked

### API Endpoints

| Method | Endpoint                             | Description                       |
| ------ | ------------------------------------ | --------------------------------- |
| GET    | `/api/notifications`                 | Get the user's notifications      |
| GET    | `/api/notifications/unread-count`    | Get count of unread notifications |
| PUT    | `/api/notifications/mark-read/{id}`  | Mark a notification as read       |
| POST   | `/api/admin/notifications/promotion` | Create a promotion notification   |

### Endpoint Data Types

#### PUT `/api/v1/notifications/unread-count/{id}`

**Request Body**

```json
{
  "id": 0,
  "title": "string",
  "content": "string",
  "type": "ORDER_STATUS_UPDATED",
  "read": true,
  "notificationDate": "2025-04-17T17:35:30.140Z",
  "referenceId": 0,
  "imageUrl": "string"
}
```

#### POST `/api/v1/notifications/promotion`

**Request Body**

```json
{
  "title": "string",
  "content": "string",
  "promotionId": 0,
  "imageUrl": "string",
  "scheduledDate": "2025-04-17T17:35:47.507Z"
}
```

**Response**

```json
{
  "id": 0,
  "title": "string",
  "content": "string",
  "type": "ORDER_STATUS_UPDATED",
  "referenceId": 0,
  "imageUrl": "string",
  "promotionProducts": [
    {
      "id": 0,
      "name": "string",
      "description": "string",
      "price": 0,
      "minPrice": 0,
      "maxPrice": 0,
      "priceWithDiscount": 0,
      "minPriceWithDiscount": 0,
      "maxPriceWithDiscount": 0,
      "categoryId": 0,
      "categoryName": "string",
      "discountRate": 0,
      "averageRating": 0,
      "numberOfReviews": 0,
      "numberOfSold": 0,
      "slug": "string",
      "colorDefault": "string",
      "images": ["string"],
      "variants": [
        {
          "id": 0,
          "color": "string",
          "size": "string",
          "quantity": 0,
          "currentUserCartQuantity": 0,
          "differencePrice": 0,
          "images": ["string"]
        }
      ],
      "featured": true
    }
  ]
}
```

#### GET `/api/v1/notifications`

**Response**

```json
{
  "notifications": [
    {
      "id": 0,
      "title": "string",
      "content": "string",
      "type": "ORDER_STATUS_UPDATED",
      "read": true,
      "notificationDate": "2025-04-17T17:37:03.407Z",
      "referenceId": 0,
      "imageUrl": "string"
    }
  ],
  "unreadCount": 0
}
```

#### GET `/api/v1/notifications/unread-count`

**Response**

```json
0
```

### Response Wrapper

All responses are wrapped in the following structure:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "Call API Success",
  "data": {}
}
```

## Frontend Integration

To integrate with the notification system, the frontend should:

1. **Connect to WebSocket**:

   ```javascript
   const socket = new SockJS("/ws");
   const stompClient = Stomp.over(socket);
   stompClient.connect({}, onConnected, onError);
   ```

2. **Subscribe to notifications**:

   ```javascript
   function onConnected() {
     // User-specific notifications
     stompClient.subscribe("/user/queue/notifications", onNotificationReceived);

     // Broadcast notifications (promotions)
     stompClient.subscribe("/topic/promotions", onPromotionReceived);
   }
   ```

3. **Handle notifications**:

   ```javascript
   function onNotificationReceived(payload) {
     const notification = JSON.parse(payload.body);
     // Display notification
     // Store in notification list
   }

   function onPromotionReceived(payload) {
     const promotion = JSON.parse(payload.body);
     // Display promotion notification with products
   }
   ```

4. **Fetch existing notifications**:

   ```javascript
   async function loadNotifications() {
     const response = await fetch("/api/notifications");
     const data = await response.json();
     // Display notifications
   }
   ```

5. **Mark notifications as read**:
   ```javascript
   async function markAsRead(notificationId) {
     await fetch(`/api/notifications/mark-read/${notificationId}`, {
       method: "PUT",
     });
     // Update UI
   }
   ```

## Security Considerations

- WebSocket connections are authenticated using the same authentication mechanism as the REST API
- Only authenticated users can access their own notifications
- Only admin users can create broadcast notifications
