# Self Progress Tracking Service - Postman Testing Guide

This guide explains how to use the provided Postman collections to test the Self Progress Tracking Service API.

## Available Collections

The project includes two Postman collections:

1. **Basic API Collection** - `docs/Self_Progress_Tracking_Service.postman_collection.json`
   - Contains all API endpoints with example requests
   - Includes test scripts to automatically set tokens

2. **30-Day Learning Journey** - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_collection.json`
   - Simulates a realistic 30-day learning journey
   - Demonstrates varying activity levels and progress patterns
   - Organized by weeks with realistic usage scenarios

## Setup Instructions

### 1. Import Collections and Environments

1. Open Postman
2. Click "Import" in the top left
3. Select the following files:
   - `docs/Self_Progress_Tracking_Service.postman_collection.json`
   - `docs/Self_Progress_Tracking_Service.postman_environment.json`
   - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_collection.json`
   - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_environment.json`

### 2. Set Environment

1. In the top right corner of Postman, select the "Self Progress Tracking Service Environment"
2. Click the eye icon to view the environment variables
3. Ensure the `baseUrl` is set to `http://localhost:8080` (or your custom URL)
4. Save any changes

## Basic API Testing

### Authentication Flow

1. **Register a User**
   - Open the "Auth" folder in the collection
   - Run the "Register" request with a valid username, email, and password
   - Note: In a real environment, you would receive an email with a verification link

2. **Verify Email**
   - Run the "Verify Email" request
   - For testing purposes, you can use the sample verification token or get the actual token from the database

3. **Login**
   - Run the "Login" request with your username and password
   - The test script will automatically extract and store the access and refresh tokens

4. **Test Protected Endpoints**
   - All subsequent requests will automatically include the access token
   - If you get a 401 Unauthorized error, try running the "Refresh Token" request

### Testing Syllabus Management

1. **Create a Syllabus**
   - Run the "Create Syllabus" request in the "Syllabus" folder
   - Note the returned syllabus ID for use in subsequent requests

2. **Add Subjects, Topics, and SubTopics**
   - Use the respective requests in each folder
   - Make sure to update the path parameters (e.g., `syllabusId`, `subjectId`, `topicId`)

3. **Track Progress**
   - Use the "Create Progress Entry" request in the "Progress" folder
   - Set the `itemId` and `itemType` to reference the item you're tracking progress for

### Testing Syllabus Sharing

1. **Create a Public Syllabus**
   - Create a syllabus with `"isPublic": true`

2. **Generate a Shareable Link**
   - Run the "Generate Shareable Link" request with the syllabus ID
   - Note the returned shareable link

3. **Access Shared Syllabus**
   - Run the "Get Syllabus by Shareable Link" request
   - This request doesn't require authentication
   - Update the path parameter with your shareable link

4. **Revoke Sharing**
   - Run the "Revoke Shareable Link" request to disable sharing

## 30-Day Learning Journey Simulation

The 30-Day Journey collection simulates a realistic learning experience over a month:

### Week 1: Getting Started
- User registration and setup
- Creating initial syllabus and subjects
- First progress entries

### Week 2: Building Momentum
- Adding more detailed topics and subtopics
- Daily progress tracking
- First analytics checks

### Week 3: Consistent Progress
- Resource attachments
- Progress updates across different items
- Streak building

### Week 4: Completion and Review
- Completing various syllabus items
- Comprehensive analytics review
- Sharing completed syllabi

### Running the Simulation

You can run the entire collection to simulate the full 30-day journey, or run specific folders to test particular weeks or scenarios.

1. Select the collection or folder
2. Click the "Run" button
3. Configure the run settings (delay between requests recommended)
4. Click "Run" to execute the requests in sequence

## Troubleshooting

- **401 Unauthorized**: Your access token may have expired. Run the "Refresh Token" request.
- **404 Not Found**: Check the URL and path parameters. Ensure the resource exists.
- **400 Bad Request**: Verify your request body matches the expected format.
- **500 Server Error**: Check the server logs for more details.

## Customizing Requests

Feel free to modify the request bodies to test different scenarios. Key areas to customize:

- User credentials in registration and login
- Syllabus, subject, topic, and subtopic details
- Progress status values (NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED)
- Date ranges for analytics requests
