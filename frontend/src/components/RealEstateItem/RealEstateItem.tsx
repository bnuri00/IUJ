import React from 'react';
import CountUp from 'react-countup';
import { RiQuestionnaireFill } from 'react-icons/ri';
import test from '../../assets/test.jpg';
import styles from './RealEstateItem.module.scss';

export type RealEstateType = {
  place: {
    name: string;
    type: string;
    address: string[];
    total_score: number;
    score: {
      [kind: string]: number;
    };
    average_deal: {
      deal_type: string;
      price: number;
      guarantee: number;
      monthly: number;
    };
    range_extent: number[];
    range_floor: number[];
  };
};

type RealEstatePropsType = {
  RE: RealEstateType;
};

function RealEstateItem({ RE }: RealEstatePropsType) {
  const pretreatAmount = (amount: number) => {
    if (amount > 10000 && amount % 10000) {
      const tmp = amount.toString();
      const result = tmp.substring(0, 1) + '억 ' + tmp.substring(1, tmp.length) + '만';
      return result;
    } else if (amount % 10000 === 0) {
      const tmp = amount.toString();
      const result = tmp.substring(0, 1) + '억';
      return result;
    } else {
      return amount.toString() + '만';
    }
  };

  return (
    <div className={styles.component}>
      <p className={styles.name}>{RE.place.name}</p>
      <div className={styles['component-inner']}>
        <div className={styles.left}>
          <div className={styles.img} style={{ backgroundImage: `url(${test})` }}>
            <p className={styles.type}>
              <span>{RE.place.type}</span> <span>{RE.place.average_deal.deal_type}</span>
            </p>
          </div>
        </div>
        <div className={styles['content']}>
          {RE.place.average_deal.deal_type === '월세' && (
            <p className={styles.price}>
              평균 {pretreatAmount(RE.place.average_deal.guarantee)} / {pretreatAmount(RE.place.average_deal.monthly)}
            </p>
          )}
          {RE.place.average_deal.deal_type === '전세' && <p className={styles.price}>평균 {pretreatAmount(RE.place.average_deal.guarantee)}</p>}
          {RE.place.average_deal.deal_type === '매매' && <p className={styles.price}>평균 {pretreatAmount(RE.place.average_deal.price)}</p>}
          <p className={styles.address}>{RE.place.address}</p>
          <p className={styles.extent}>
            전용면적(㎡): {RE.place.range_extent[0]} ~ {RE.place.range_extent[1]}
          </p>
          <p className={styles.floor}>
            층수: {RE.place.range_floor[0]} ~ {RE.place.range_floor[1]}
          </p>
          <p className={styles.score}>
            <span>추천 점수: </span>
            <div>
              {RE.place.total_score < 70 && <CountUp end={RE.place.total_score} duration={1} decimals={2} decimal="." />}
              {RE.place.total_score >= 70 && RE.place.total_score < 90 && (
                <CountUp end={RE.place.total_score} duration={1} decimals={2} decimal="." style={{ fontWeight: '600' }} />
              )}
              {RE.place.total_score >= 90 && (
                <CountUp end={RE.place.total_score} duration={1} decimals={2} decimal="." style={{ color: 'rgba(255, 148, 148, 1)', fontWeight: '600' }} />
              )}
            </div>
            <RiQuestionnaireFill />
          </p>
        </div>
      </div>
    </div>
  );
}

export default RealEstateItem;
