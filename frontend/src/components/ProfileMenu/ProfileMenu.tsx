import React, { useEffect, useRef, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { setUserLogoutState } from '../../store/slices/userSlice';
import styles from './ProfileMenu.module.scss';

type ProfileMenuPropsType = {
  setShowProfileMenu: React.Dispatch<React.SetStateAction<boolean>>;
};

function ProfileMenu({ setShowProfileMenu }: ProfileMenuPropsType) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setShowProfileMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [menuRef]);

  /** 관심 매물 목록으로 */
  const onClickInterest = () => {
    navigate('/interest');
  };

  /** 로그아웃 */
  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userName');
    dispatch(setUserLogoutState());
  };

  return (
    <div ref={menuRef} className={styles.component}>
      <div onClick={onClickInterest}>관심 매물 목록</div>
      <div onClick={logout}>로그아웃</div>
    </div>
  );
}

export default ProfileMenu;
