const fs = require('fs');
const path = require('path');
const https = require('https');

const usernames = [
  'Adriano-Queiroz',
  'BCorreia02',
  'Calmskyy',
  'FlavioMiguel27',
  'Lisboa8045',
  'Shrimpo22'
];

const downloadImage = (url, filepath) => {
  return new Promise((resolve, reject) => {
    const file = fs.createWriteStream(filepath);
    https.get(url, response => {
      response.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', reject);
  });
};

const fetchGitHubAvatar = (username) => {
  return new Promise((resolve, reject) => {
    https.get(`https://api.github.com/users/${username}`, {
      headers: { 'User-Agent': 'node.js' }
    }, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const json = JSON.parse(data);
        resolve(json.avatar_url);
      });
    }).on('error', reject);
  });
};

(async () => {
  const outputDir = path.join(__dirname, '..', 'public', 'assets', 'team');
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  for (const username of usernames) {
    try {
      console.log(`Fetching avatar for ${username}`);
      const avatarUrl = await fetchGitHubAvatar(username);
      const filePath = path.join(outputDir, `${username}.png`);
      await downloadImage(avatarUrl, filePath);
      console.log(`✅ Saved: ${filePath}`);
    } catch (err) {
      console.error(`❌ Failed for ${username}`, err);
    }
  }
})();
