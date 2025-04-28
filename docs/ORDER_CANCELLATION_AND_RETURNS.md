# Order Cancellation and Return System

This document outlines the implementation of order cancellation and product return functionality for the e-commerce fashion application.

## 1. Order Cancellation

### Features

- Users can cancel orders in the `PENDING` status (not yet confirmed)
- Cancellation reason can be provided by the user
- Stock is automatically returned to inventory
- Support for both COD and VNPAY payment methods

### Implementation Details

- Created `userCancelOrder` method in `OderCancellationService`
- Added cancellation endpoint in `OrderController`
- Implemented `OrderCancelReqDTO` for request data transfer
- Cancellation endpoint: `PUT /api/v1/orders/user/cancel`

### Security

- Validates that the user canceling the order is the owner
- Only allows cancellation of orders in PENDING status

## 2. Return Request System

### Features

- Users can create return requests for delivered orders
- Users can upload up to 3 images of the returned product
- Return reason must be provided
- For COD orders, users must provide bank account details for refund
- For VNPAY orders, refund will be processed to the original payment method
- Admin can approve or reject return requests

### Entities

- `ReturnRequest`: Stores return request information including reason, status, and refund information
- `ReturnRequestImage`: Stores image URLs for return requests
- `ReturnRequestStatus`: Enum with values PENDING, APPROVED, REJECTED

### API Endpoints

#### User Endpoints

- Create return request: `POST /api/v1/return-requests/user`
- Get return request details: `GET /api/v1/return-requests/{id}`
- Delete a return request (only in PENDING status): `DELETE /api/v1/return-requests/{id}`
- Upload return images: `POST /api/v1/return-images/upload-images`

#### Admin Endpoints

- View all return requests: `GET /api/v1/return-requests`
- Process return request: `PUT /api/v1/return-requests/process`

### Image Upload Enhancement

- Enhanced `CloudStorageService` to support directory customization
- Created a dedicated `ImageController` for file uploads with directory functionality
- Added specific endpoint for return images with proper storage path
- Allows uploading up to 3 images per return request

## 3. Security Considerations

- All endpoints require authentication
- Data validation for input parameters
- Authorization checks to ensure users can only access their own orders and return requests
- Admin-only endpoints protected with appropriate role checks

## 4. Data Flow

### Order Cancellation

1. User submits cancellation request with order ID and optional reason
2. System validates order status and ownership
3. Order status is changed to CANCELLED
4. Stock is returned to inventory
5. Success response is sent to user

### Return Request

1. User creates return request with order ID, reason, images, and bank details (if COD)
2. System validates order status (must be DELIVERED) and ownership
3. Images are uploaded to Google Cloud Storage with signed URL in the return-images directory
4. Return request is created with PENDING status
5. Admin reviews return request and approves/rejects
6. If approved, order status is updated to RETURNED and refund process is initiated
7. For VNPAY orders, refund is processed to the original payment method
8. For COD orders, refund is processed to the provided bank account

## 5. Technical Implementation

- Used JPA for entity management
- Implemented DTOs for data transfer between layers
- Created service interfaces and implementations
- Added REST controllers with appropriate HTTP methods
- Enhanced existing CloudStorageService for file uploads
- Integrated with the existing order and payment systems
- Added proper error handling and validation
