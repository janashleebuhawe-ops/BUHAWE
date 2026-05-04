<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>LMS | Terminal Login</title>
    <link rel="stylesheet" th:href="@{/style.css}">
    <script th:src="@{/auth.js}" defer></script>
</head>
<body>
    <div class="login-container">
        <div class="logo-icon">💻</div>
        <h2>Welcome back!</h2>
        
        <form id="loginForm">
            <div class="form-group">
                <label>Username</label>
                <input type="text" id="username" placeholder="Enter your username" required>
            </div>
            
            <div class="form-group">
                <label>Password</label>
                <input type="password" id="password" placeholder="Enter your password" required>
            </div>
            
            <button type="submit">Login</button>
        </form>
        
        <div id="message"></div>
        
        <div class="footer-text">
            Don't have an account? <a href="#">Register here</a>
        </div>
    </div>
</body>
</html>