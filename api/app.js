const express = require('express');
const mysql = require('mysql');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const path = require('path');

const app = express();
const port = process.env.PORT || 3000;

// MySQL bağlantı bilgileri
const db = mysql.createConnection({
    host: "127.0.0.1",
    user: "root",
    password: "password",
    database: "deneme",
    port: "3306"
});

// MySQL bağlantısını başlatma
db.connect((err) => {
    if (err) {
        throw err;
    }
    console.log("MySQL veritabanına başarıyla bağlandı.");
});

// Express Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Register endpoint'i
app.post('/register', async (req, res) => {
    try {
        const { user_email_address, user_password, user_name } = req.body;

        // Şifreyi bcrypt ile şifreleme
        const hashedPassword = await bcrypt.hash(user_password, 10);

        // Kullanıcıyı veritabanına ekleme
        db.query('INSERT INTO users (user_email_address, user_password, user_name) VALUES (?, ?, ?)', 
            [user_email_address, hashedPassword, user_name], 
            (err, result) => {
                if (err) {
                    console.error(err);
                    res.status(500).send("Kullanıcı oluşturulurken bir hata oluştu.");
                } else {
                    res.status(201).send("Kullanıcı başarıyla oluşturuldu.");
                }
            }
        );
    } catch (error) {
        console.error(error);
        res.status(500).send("Bir hata oluştu.");
    }
});

// Login endpoint'i
app.post('/login', async (req, res) => {
    try {
        const { user_email_address, user_password } = req.body;

        // Kullanıcıyı veritabanından bulma
        db.query('SELECT * FROM users WHERE user_email_address = ?', [user_email_address], async (err, result) => {
            if (err) {
                console.error(err);
                res.status(500).send("Giriş yapılırken bir hata oluştu.");
            } else {
                if (result.length > 0) {
                    const user = result[0];
                    // Şifreyi karşılaştırma
                    const passwordMatch = await bcrypt.compare(user_password, user.user_password);
                    if (passwordMatch) {
                        // JWT token oluşturma
                        const token = jwt.sign({ user_id: user.user_id }, 'secretkey');
                        res.status(200).json({ token });
                    } else {
                        res.status(401).send("Hatalı e-posta veya şifre.");
                    }
                } else {
                    res.status(404).send("Hatalı e-posta veya şifre.");
                }
            }
        });
    } catch (error) {
        console.error(error);
        res.status(500).send("Bir hata oluştu.");
    }
});

// Tüm içecekleri getiren endpoint
app.get('/drinks', (req, res) => {
    const query = 'SELECT * FROM deneme.drinks';
    db.query(query, (err, results) => {
        if (err) {
            res.status(500).json({ message: 'İçecekler alınamadı' });
            throw err;
        }
        res.json(results);
    });
});

// İçecek detaylarını getiren endpoint
app.get('/drinks/:id', (req, res) => {
    const id = req.params.id;
    const query = 'SELECT * FROM deneme.drinks WHERE id = ?';
    db.query(query, [id], (err, results) => {
        if (err) {
            res.status(500).json({ message: 'İçecek detayları alınamadı' });
            throw err;
        }
        if (results.length === 0) {
            res.status(404).json({ message: 'İçecek bulunamadı' });
            return;
        }
        res.json(results[0]);
    });
});

// İçecek resmini getiren endpoint
app.get('/drinks/:id/image', (req, res) => {
    const id = req.params.id;
    const query = 'SELECT image_url FROM deneme.drinks WHERE id = ?';
    db.query(query, [id], (err, results) => {
        if (err) {
            res.status(500).json({ message: 'İçecek resmi alınamadı' });
            throw err;
        }
        if (results.length === 0 || !results[0].image_url) {
            res.status(404).json({ message: 'İçecek resmi bulunamadı' });
            return;
        }
        const imagePath = path.join(__dirname, results[0].image_url);
        res.sendFile(imagePath);
    });
});

// Tüm sporcuları getiren endpoint
app.get('/athletes', (req, res) => {
    const query = 'SELECT * FROM athletes';
    db.query(query, (err, results) => {
        if (err) {
            res.status(500).json({ message: 'Veriler alınamadı' });
            throw err;
        }
        res.json(results);
    });
});

// Sepete ürün ekleme endpoint'i
app.post('/basket/add', (req, res) => {
    const { user_id, drink_id, adet ,is_emty} = req.body;
    const query = 'INSERT INTO deneme.basket (user_id, drink_id, adet,is_emty) VALUES (?, ?, ?,?)';
    db.query(query, [user_id, drink_id, adet,1], (err, result) => {
        if (err) {
            res.status(500).json({ message: 'Sepete ürün eklenirken bir hata oluştu' });
            throw err;
        }
        res.json({ message: 'Ürün başarıyla sepete eklendi' });
    });
});

// Sepeti görüntüleme endpoint'i
app.get('/basket/show/:user_id', (req, res) => {
    const user_id = req.params.user_id;
    const query = `
        SELECT s.name, s.description, s.image_url, b.adet
        FROM deneme.basket b
        INNER JOIN deneme.drinks s ON b.drink_id = s.id 
        WHERE b.user_id = ?
    `;
    db.query(query, [user_id], (err, results) => {
        if (err) {
            res.status(500).json({ message: 'Sepet alınamadı' });
            throw err;
        }
        res.json(results);
    });
});

// Basket güncelleme endpoint'i
app.post('/basket/update', (req, res) => {
    const { user_id, drink_id, adet } = req.body;
    
    // Mevcut adeti almak için SELECT sorgusu yapılıyor
    const selectQuery = 'SELECT adet FROM basket WHERE user_id = ? AND drink_id = ?';
    db.query(selectQuery, [user_id, drink_id], (err, rows) => {
        if (err) {
            res.status(500).json({ message: 'Sepet güncellenirken bir hata oluştu' });
            throw err;
        }
        
        if (rows.length === 0) {
            res.status(404).json({ message: 'Ürün sepetinizde bulunamadı' });
            return;
        }
        
        const currentQuantity = rows[0].adet;
        
        // Yeni adet değerini hesaplıyoruz
        let updatedQuantity = adet;

        // Yeni adet değerine göre is_emty değerini ayarlıyoruz
        const is_emty = updatedQuantity > 0 ? 1 : 0;

        // Güncelleme sorgusu yapılıyor
        const updateQuery = 'UPDATE basket SET adet = ?, is_emty = ? WHERE user_id = ? AND drink_id = ?';
        db.query(updateQuery, [updatedQuantity, is_emty, user_id, drink_id], (err, result) => {
            if (err) {
                res.status(500).json({ message: 'Sepet güncellenirken bir hata oluştu' });
                throw err;
            }
            res.json({ message: 'Sepet başarıyla güncellendi' });
        });
    });
});

// API'nin dinlemeye başlaması
app.listen(port, () => {
    console.log(`API çalışıyor: http://localhost:${port}`);
});
