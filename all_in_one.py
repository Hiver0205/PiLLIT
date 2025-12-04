import os
import requests
import json

# =============================
# 환경 설정
# =============================
IMG_DIR = "test_images"
GT_DIR = "ground_truth"
RESULT_DIR = "results"

OCR_API_URL = "https://scaneat-ocr-server.onrender.com/ocr"

os.makedirs(RESULT_DIR, exist_ok=True)


# =============================
# 유저 알러지 단계 → 위험도 변환 함수
# =============================
def risk_from_userlevel(level):
    if level == "중증":
        return "DANGER"
    elif level == "경증":
        return "WARNING"
    return "SAFETY"


# =============================
# FastAPI OCR 서버 호출
# =============================
def run_ocr(image_path):
    with open(image_path, "rb") as f:
        files = {"file": f}
        response = requests.post(OCR_API_URL, files=files)

    if response.status_code == 200:
        return response.json().get("text", "")
    else:
        print("❌ 서버 오류:", response.status_code)
        return ""


# =============================
# 메인: 모든 이미지 처리
# =============================
def main():

    images = sorted(os.listdir(IMG_DIR))

    for img in images:
        if not img.lower().endswith((".png", ".jpg", ".jpeg")):
            continue

        base = os.path.splitext(img)[0]
        image_path = os.path.join(IMG_DIR, img)

        print(f"\n=============================")
        print(f"[처리중] {img}")
        print("=============================")

        # 1) OCR 실행
        ocr_text = run_ocr(image_path)
        print("OCR 결과:", ocr_text)

        # 2) userlevel 불러오기
        userlevel_path = os.path.join(GT_DIR, f"{base}_userlevel.txt")
        if not os.path.exists(userlevel_path):
            print(f"❌ userlevel 파일 없음 → {userlevel_path}")
            continue

        user_level = open(userlevel_path, encoding="utf-8").read().strip()
        print("user_level:", user_level)

        # 3) 위험도 계산
        predicted_risk = risk_from_userlevel(user_level)
        print("예측 위험도:", predicted_risk)

        # 4) 결과 저장
        risk_path = os.path.join(RESULT_DIR, f"{base}_risk_pred.txt")
        with open(risk_path, "w", encoding="utf-8") as f:
            f.write(predicted_risk)

        print(f"[SAVE] 위험도 → {risk_path}")

    print("\n🎉 모든 이미지 처리 완료!")


if __name__ == "__main__":
    main()
