INSERT IGNORE INTO global_configuration (id, config_key, config_value, config_type, created_at, updated_at, created_by,
                                         updated_by)
VALUES (1, 'confirmation_token_expiry_minutes', 60, 'INT', NOW(), NOW(), 'system', 'system'),
       (2, 'email', 'academy.project.do.not.reply@gmail.com', 'STRING', NOW(), NOW(), 'system', 'system'),
       (3, 'password', 'hehixeicdqmkvjjo', 'STRING', NOW(), NOW(), 'system', 'system'),
       (4, 'password_reset_token_expiry_minutes', 20, 'INT', NOW(), NOW(), 'system', 'system'),
       (5, 'terms_and_conditions', '<h2>Terms and Conditions</h2>
<p><strong>Effective Date:</strong> July 6, 2025</p>

<p>Welcome to <strong>AcademyProject</strong>! These Terms and Conditions govern your access to and use of our platform. By creating an account or using any part of our services, you agree to be bound by these terms.</p>

<h3>1. Description of Service</h3>
<p>AcademyProject is a non-commercial, training-oriented online marketplace for local services including but not limited to beauty parlors, barbers, sports field rentals, and gym training providers. Users may browse, list, and request services through the platform.</p>

<h3>2. Account Requirements</h3>
<ul>
  <li>Both clients and service providers must register for an account to access core features.</li>
  <li>All users must be at least 13 years old to use the platform.</li>
</ul>

<h3>3. User Responsibilities</h3>
<ul>
  <li>You agree to provide accurate, complete, and updated information when registering and using the service.</li>
  <li>You are responsible for maintaining the confidentiality of your login credentials.</li>
  <li>You agree not to misuse the platform or engage in fraudulent or illegal activities.</li>
</ul>

<h3>4. Data Collection</h3>
<p>We collect email addresses, physical addresses, and phone numbers for account creation, communication, and service facilitation. See our Privacy Policy for more information.</p>

<h3>5. Cookies</h3>
<p>We use essential cookies solely for technical functionality, such as enabling auto-login features. No analytics or tracking cookies are used.</p>

<h3>6. Intellectual Property</h3>
<p>All content, trademarks, logos, and materials presented on AcademyProject are the intellectual property of their respective owners and may not be reused without permission.</p>

<h3>7. International Use</h3>
<p>Although the platform primarily targets users in Portugal, it may be accessed internationally. Users are responsible for complying with local laws and regulations.</p>

<h3>8. Disclaimer</h3>
<p>AcademyProject is a non-commercial training project. While we strive to ensure the quality and accuracy of information on the site, we do not guarantee the reliability, availability, or legality of any listed services.</p>

<h3>9. Contact</h3>
<p>If you have any questions regarding these terms, you may contact us at: <a href="mailto:academy.project.do.not.reply@gmail.com">academy.project.do.not.reply@gmail.com</a></p>
', 'STRING', NOW(), NOW(), 'system', 'system'),
       (6, 'privacy_policy', '<h2>Privacy Policy</h2>
<p><strong>Effective Date:</strong> July 6, 2025</p>

<p>AcademyProject is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and protect your information when you use our platform.</p>

<h3>1. Information We Collect</h3>
<ul>
  <li><strong>Email Address:</strong> for account registration and communication.</li>
  <li><strong>Phone Number:</strong> for service coordination and account verification.</li>
  <li><strong>Address:</strong> to help users locate and connect with nearby service providers.</li>
</ul>

<h3>2. How We Use Your Information</h3>
<ul>
  <li>To register and manage user accounts.</li>
  <li>To facilitate communication between clients and service providers.</li>
  <li>To provide platform functionality such as auto-login via cookies.</li>
</ul>

<h3>3. Cookies</h3>
<p>We use cookies strictly for functional purposes — for example, to remember login sessions. We do not use cookies for analytics, marketing, or third-party tracking.</p>

<h3>4. Data Sharing</h3>
<p>We do not sell or share your personal information with third parties. Any data shared within the platform is strictly for enabling services between users.</p>

<h3>5. Data Security</h3>
<p>We take reasonable technical and organizational measures to protect your data. However, no platform can guarantee 100% security.</p>

<h3>6. International Users</h3>
<p>Although our service is mainly targeted at Portugal, users from other countries may access it. By using our platform, you consent to your data being processed in accordance with this policy.</p>

<h3>7. Your Rights</h3>
<ul>
  <li>You may request access, correction, or deletion of your personal data at any time.</li>
  <li>You may deactivate your account by contacting us.</li>
</ul>

<h3>8. Children</h3>
<p>AcademyProject is intended for users aged 13 and older. We do not knowingly collect personal data from children under 13.</p>

<h3>9. Contact</h3>
<p>If you have any questions about this Privacy Policy, you may contact us at: <a href="mailto:academy.project.do.not.reply@gmail.com">academy.project.do.not.reply@gmail.com</a></p>

', 'STRING', NOW(), NOW(), 'system', 'system'),
       (7, 'about_me', '<h2>About AcademyProject</h2>

<p>Welcome to AcademyProject, your local marketplace for discovering and connecting with the best service providers in your community. Whether you''re looking for a trusted beauty parlor, a skilled barber, a nearby gym trainer, or a sports field to rent, we bring all these services right to your fingertips.</p>

<p>Our mission is simple: to make it easy for people to find great local services and for service providers to showcase what they offer. We want you to spend less time searching and more time enjoying what you love.</p>

<p>Here at AcademyProject, you can browse through a variety of services, check out profiles, and get in touch directly. It’s all about making connections that matter.</p>

<p>Feel free to explore and find what fits your needs — we’re here to help you discover something awesome nearby.</p>

<h3>Meet the Team</h3>
<p>Here are the amazing folks who made AcademyProject happen:</p>

<div class="team-members">
  <a href="https://github.com/Adriano-Queiroz" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/Adriano-Queiroz.png" alt="Adriano-Queiroz" />
    <div class="username">Adriano-Queiroz</div>
  </a>
  <a href="https://github.com/BCorreia02" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/BCorreia02.png" alt="BCorreia02" />
    <div class="username">BCorreia02</div>
  </a>
  <a href="https://github.com/Calmskyy" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/Calmskyy.png" alt="Calmskyy" />
    <div class="username">Calmskyy</div>
  </a>
  <a href="https://github.com/FlavioMiguel27" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/FlavioMiguel27.png" alt="FlavioMiguel27" />
    <div class="username">FlavioMiguel27</div>
  </a>
  <a href="https://github.com/Lisboa8045" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/Lisboa8045.png" alt="Lisboa8045" />
    <div class="username">Lisboa8045</div>
  </a>
  <a href="https://github.com/Shrimpo22" target="_blank" rel="noopener noreferrer" class="avatar-link">
    <img src="assets/team/Shrimpo22.png" alt="Shrimpo22" />
    <div class="username">Shrimpo22</div>
  </a>
</div>
', 'STRING', NOW(), NOW(), 'system', 'system');
