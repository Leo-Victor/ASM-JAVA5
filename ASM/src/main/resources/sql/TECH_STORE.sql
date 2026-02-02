IF NOT EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'TECH_STORE')
BEGIN
    CREATE DATABASE TECH_STORE;
END
GO

USE TECH_STORE;
GO

-- 1. Bảng Accounts (Người dùng)
CREATE TABLE Accounts (
    Username NVARCHAR(50) NOT NULL PRIMARY KEY,
    Password NVARCHAR(50) NOT NULL,
    Fullname NVARCHAR(50) NOT NULL,
    Email NVARCHAR(50) NOT NULL,
    Photo NVARCHAR(50),
    Activated BIT NOT NULL DEFAULT 1, -- 1: Kích hoạt, 0: Khóa
    Admin BIT NOT NULL DEFAULT 0      -- 1: Admin, 0: User
);

-- 2. Bảng Categories (Loại hàng)
CREATE TABLE Categories (
    Id CHAR(4) NOT NULL PRIMARY KEY,
    Name NVARCHAR(50) NOT NULL
);

-- 3. Bảng Products (Hàng hóa)
CREATE TABLE Products (
    Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Image NVARCHAR(50),
    Price FLOAT NOT NULL,
    CreateDate DATE NOT NULL DEFAULT GETDATE(),
    Available BIT NOT NULL DEFAULT 1,
    CategoryId CHAR(4) NOT NULL,
    FOREIGN KEY (CategoryId) REFERENCES Categories(Id)
);

-- 4. Bảng Orders (Đơn hàng)
CREATE TABLE Orders (
    Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL,
    CreateDate DATETIME NOT NULL DEFAULT GETDATE(),
    Address NVARCHAR(100) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Accounts(Username)
);

-- 5. Bảng OrderDetails (Chi tiết đơn hàng)
CREATE TABLE OrderDetails (
    Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    OrderId BIGINT NOT NULL,
    ProductId INT NOT NULL,
    Price FLOAT NOT NULL,
    Quantity INT NOT NULL,
    FOREIGN KEY (OrderId) REFERENCES Orders(Id),
    FOREIGN KEY (ProductId) REFERENCES Products(Id)
);

-- --- NHẬP DỮ LIỆU MẪU (DATA SAMPLE) ---

-- User & Admin
INSERT INTO Accounts (Username, Password, Fullname, Email, Activated, Admin) VALUES 
('admin', '123', N'Nguyễn Văn Quản Lý', 'admin@poly.edu.vn', 1, 1),
('user', '123', N'Trần Thị Khách Hàng', 'user@poly.edu.vn', 1, 0);

-- Categories (Loại hàng)
INSERT INTO Categories (Id, Name) VALUES 
('1000', N'Laptop'),
('1001', N'Điện thoại'),
('1002', N'Máy tính bảng'),
('1003', N'Đồng hồ thông minh');

-- Products (Sản phẩm)
INSERT INTO Products (Name, Price, CreateDate, Available, CategoryId, Image) VALUES 
(N'Laptop Gaming Asus', 25000000, '2025-10-20', 1, '1000', 'product1.jpg'),
(N'iPhone 15 Pro Max', 30000000, '2025-11-15', 1, '1001', 'product2.jpg'),
(N'Samsung Galaxy Tab', 12000000, '2025-12-01', 1, '1002', 'product3.jpg'),
(N'Apple Watch Series 9', 10000000, '2026-01-10', 1, '1003', 'product4.jpg');



GO

-- 1. Tạo đơn hàng thứ nhất (Của khách 'user')
INSERT INTO Orders (Username, CreateDate, Address)
VALUES ('user', GETDATE(), N'123 Đường Lê Lợi, Quận 1, TP.HCM');

-- 2. Tạo đơn hàng thứ hai (Của khách 'admin')
INSERT INTO Orders (Username, CreateDate, Address)
VALUES ('admin', GETDATE(), N'456 Đường Nguyễn Huệ, Hà Nội');

-- (Tùy chọn) Thêm chi tiết đơn hàng (Mua món gì)
-- Giả sử Order vừa tạo có ID là 1 và 2, Product có ID là 1
INSERT INTO OrderDetails (OrderId, ProductId, Price, Quantity) VALUES (1, 1, 25000000, 1);
INSERT INTO OrderDetails (OrderId, ProductId, Price, Quantity) VALUES (2, 1, 30000000, 2);




-- THÊM DỮ LIỆU SẢN PHẨM MỚI
INSERT INTO Products (Name, Image, Price, CreateDate, Available, CategoryId) VALUES 
(N'MacBook Air M2 2023', 'macbook-air-m2.jpg', 26990000, GETDATE(), 1, '1000'),
(N'Laptop Dell XPS 13', 'dell-xps-13.jpg', 35000000, GETDATE(), 1, '1000'),
(N'Laptop HP Envy 13', 'hp-envy-13.jpg', 18500000, '2025-12-20', 1, '1000'),
(N'Asus ROG Strix G16', 'asus-rog.jpg', 32000000, GETDATE(), 1, '1000'),
(N'Lenovo ThinkPad X1', 'thinkpad-x1.jpg', 42000000, '2025-11-10', 1, '1000'),

(N'iPhone 15 Pro Max Titan', 'iphone15-prm.jpg', 34990000, GETDATE(), 1, '1001'),
(N'iPhone 14 Plus', 'iphone14-plus.jpg', 21990000, '2025-05-15', 1, '1001'),
(N'Samsung Galaxy S24 Ultra', 's24-ultra.jpg', 31000000, GETDATE(), 1, '1001'),
(N'Samsung Galaxy Z Fold5', 'z-fold-5.jpg', 40990000, '2025-10-01', 1, '1001'),
(N'Xiaomi 14 Ultra', 'xiaomi-14.jpg', 24990000, GETDATE(), 1, '1001'),

(N'iPad Pro M4 11 inch', 'ipad-pro-m4.jpg', 23990000, GETDATE(), 1, '1002'),
(N'iPad Air 6 M2', 'ipad-air-6.jpg', 16990000, GETDATE(), 1, '1002'),
(N'Samsung Galaxy Tab S9', 'tab-s9.jpg', 19990000, '2025-09-05', 1, '1002'),
(N'Lenovo Tab P11', 'lenovo-tab.jpg', 6500000, '2025-08-20', 1, '1002'),

(N'Apple Watch Ultra 2', 'watch-ultra-2.jpg', 21990000, GETDATE(), 1, '1003'),
(N'Apple Watch Series 9', 'watch-s9.jpg', 10490000, '2025-12-01', 1, '1003'),
(N'Garmin Fenix 7 Pro', 'garmin-7.jpg', 24500000, '2025-07-22', 1, '1003'),
(N'Samsung Galaxy Watch 6', 'galaxy-watch-6.jpg', 6990000, '2025-09-15', 1, '1003');


USE TECH_STORE;
GO

CREATE TABLE chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender NVARCHAR(255) NOT NULL,
    receiver NVARCHAR(255),
    content NVARCHAR(MAX),
    timestamp DATETIME DEFAULT GETDATE()
);
GO