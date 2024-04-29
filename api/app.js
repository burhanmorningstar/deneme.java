const express = require('express');
const mysql = require('mysql');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const path = require('path');

const app = express();
const port = process.env.PORT || 8080;



// MySQL ba lant  bilgileri
const db = mysql.createConnection({
    host: "127.0.0.1",
    user: "root",
    password: "yeni_parola",
    database: "www_mobil_data",
    port: "3306"
});

// MySQL ba lant s n  ba latma
db.connect((err) => {
    if (err) {
        throw err;
    }
    console.log("MySQL veritabanına başarıyla bağlandı.");
});

// Express Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/isApiWorking', (req, res) => {
    res.status(200).send('API çalışıyor!');
  });

// Register endpoint'i
app.post('/register', async (req, res) => {
    try {
        const { user_email_address, user_password, user_name } = req.body;

        //  ifreyi bcrypt ile  ifreleme
        const hashedPassword = await bcrypt.hash(user_password, 10);

        // Kullan c y  veritaban na ekleme
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
                return;
            }

            if (result.length > 0) {
                const user = result[0];
                // Şifreyi karşılaştırma
                const passwordMatch = await bcrypt.compare(user_password, user.user_password);
                if (passwordMatch) {
                    res.status(200).json({ userId: user.user_id });
                } else {
                    res.status(401).send("Hatalı e-posta veya şifre.");
                }
            } else {
                res.status(404).send("Hatalı e-posta veya şifre.");
            }
        });
    } catch (error) {
        console.error(error);
        res.status(500).send("Bir hata oluştu.");
    }
});


// T m i ecekleri getiren endpoint
app.get('/drinks', (req, res) => {
    const query = 'SELECT * FROM drinks';
    db.query(query, (err, results) => {
        if (err) {
            res.status(500).json({ message: ' içecekler alınamadı' });
            throw err;
        }
        res.json(results);
    });
});

//   ecek detaylar n  getiren endpoint
app.get('/drinks/:id', (req, res) => {
    const id = req.params.id;
    const query = 'SELECT * FROM drinks WHERE id = ?';
    db.query(query, [id], (err, results) => {
        if (err) {
            res.status(500).json({ message: 'içecek detayları alınamadı' });
            throw err;
        }
        if (results.length === 0) {
            res.status(404).json({ message: 'içecek bulunamadı' });
            return;
        }
        res.json(results[0]);
    });
});

//   ecek resmini getiren endpoint
app.get('/drinks/:id/image', (req, res) => {
    const id = req.params.id;
    const query = 'SELECT image_url FROM drinks WHERE id = ?';
    db.query(query, [id], (err, results) => {
        if (err) {
            res.status(500).json({ message: 'içecek resmi alınamadı' });
            throw err;
        }
        if (results.length === 0 || !results[0].image_url) {
            res.status(404).json({ message: 'içecek resmi bulunamadı' });
            return;
        }
        const imagePath = path.join(__dirname, results[0].image_url);
        res.sendFile(imagePath);
    });
});

// T m sporcular  getiren endpoint
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

// Sepete  r n ekleme endpoint'i
app.post('/basket/add', (req, res) => {
    const { user_id, drink_id } = req.body;
    const querySelect = 'SELECT quantity FROM basket WHERE user_id = ? AND drink_id = ?';
    const queryInsert = 'INSERT INTO basket (user_id, drink_id, quantity, is_empty) VALUES (?, ?, ?, ?)';
    const queryUpdate = 'UPDATE basket SET quantity = ? WHERE user_id = ? AND drink_id = ?';

    // Önce belirli bir kullanıcının belirli bir içeceği sepetine eklenip eklenmediğini kontrol ediyoruz
    db.query(querySelect, [user_id, drink_id], (err, rows) => {
        if (err) {
            res.status(500).json({ message: 'Sepet kontrol edilirken bir hata oluştu' });
            throw err;
        }

        // Eğer belirli bir içecek daha önce eklenmemişse, yeni bir giriş oluşturun
        if (rows.length === 0) {
            db.query(queryInsert, [user_id, drink_id, 1, 1], (err, result) => {
                if (err) {
                    res.status(500).json({ message: 'Sepete ürün eklenirken bir hata oluştu' });
                    throw err;
                }
                res.json({ message: 'Ürün başarıyla sepete eklendi' });
            });
        } else {
            // Eğer belirli bir içecek daha önce eklenmişse, mevcut quantity değerini arttırın
            const currentQuantity = rows[0].quantity;
            const newQuantity = currentQuantity + 1;
            db.query(queryUpdate, [newQuantity, user_id, drink_id], (err, result) => {
                if (err) {
                    res.status(500).json({ message: 'Sepet güncellenirken bir hata oluştu' });
                    throw err;
                }
                res.json({ message: 'Ürün başarıyla sepete eklendi' });
            });
        }
    });
});


// Sepeti g r nt leme endpoint'i
app.get('/basket/show/:user_id', (req, res) => {
    const user_id = req.params.user_id;
    const query = `
        SELECT s.name, s.description, s.image_url, b.quantity
        FROM basket b
        INNER JOIN drinks s ON b.drink_id = s.id 
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

// Basket g ncelleme endpoint'i
app.post('/basket/update', (req, res) => {
    const { user_id, drink_id, quantity } = req.body;
    
    // Mevcut quantityi almak i in SELECT sorgusu yap l yor
    const selectQuery = 'SELECT quantity FROM basket WHERE user_id = ? AND drink_id = ?';
    db.query(selectQuery, [user_id, drink_id], (err, rows) => {
        if (err) {
            res.status(500).json({ message: 'Sepet güncellenirken bir hata oluştu' });
            throw err;
        }
        
        if (rows.length === 0) {
            res.status(404).json({ message: 'ürün sepetinizde bulunamadı' });
            return;
        }
        
        const currentQuantity = rows[0].quantity;
        
        // Yeni quantity de erini hesapl yoruz
        let updatedQuantity = quantity;

        // Yeni quantity de erine g re is_empty de erini ayarl yoruz
        const is_empty = updatedQuantity > 0 ? 1 : 0;

        // G ncelleme sorgusu yap l yor
        const updateQuery = 'UPDATE basket SET quantity = ?, is_empty = ? WHERE user_id = ? AND drink_id = ?';
        db.query(updateQuery, [updatedQuantity, is_empty, user_id, drink_id], (err, result) => {
            if (err) {
                res.status(500).json({ message: 'Sepet güncellenirken bir hata oluştu' });
                throw err;
            }
            res.json({ message: 'Sepet başarıyla güncellendi' });
        });
    });
});

// API'nin dinlemeye ba lamas 
app.listen(port, () => {
    console.log(`API çalışyor: http://93.95.26.206:${port}`);
});
