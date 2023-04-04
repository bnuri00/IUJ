import React, { useEffect } from 'react';
import googleLoginBtn from '../../assets/google_login.png';
import styles from './LoginContainer.module.scss';

function LoginContainer() {
  const OAuth = async () => {
    try {
      const url = 'http://localhost:5000/oauth2/authorization/google';
      // const url = 'https://j8e103.p.ssafy.io:5000/oauth2/authorization/google';
      window.location.href = url;
    } catch (err) {
      console.error(err);
    }
  };

  const onClickGoogleLoginBtn = () => {
    OAuth();
  };

  return (
    <div>
      <div className={styles.container}>
        <div className={styles.title}>
          <h1>나의 맞춤 매물을</h1>
          <h1>찾아보세요</h1>
          <img src={googleLoginBtn} alt="google-login-btn" onClick={onClickGoogleLoginBtn} />
        </div>
      </div>
    </div>
  );
}

export default LoginContainer;
