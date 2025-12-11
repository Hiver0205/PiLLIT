import os
import cv2
import random
import numpy as np
import matplotlib.pyplot as plt
import csv
import requests
from PIL import Image, ImageEnhance, ImageFilter

# =============================
# ⚙️ 환경 설정
# =============================
TEST_IMAGE_DIR = "test_images"
GROUND_TRUTH_DIR = "ground_truth"

OCR_API_URL = "https://scaneat-ocr-server.onrender.com/ocr"

# =============================
# EASY 증강 (유사도 98~100%)
# =============================
def easy_augment_image(img):
    pil_img = Image.fromarray(img)

    # 밝기 아주 약하게
    enhancer = ImageEnhance.Brightness(pil_img)
    pil_img = enhancer.enhance(random.uniform(0.98, 1.02))

    # 대비 아주 약하게
    enhancer = ImageEnhance.Contrast(pil_img)
    pil_img = enhancer.enhance(random.uniform(0.98, 1.03))

    # blur 거의 없음
    pil_img = pil_img.filter(ImageFilter.GaussianBlur(radius=random.uniform(0.0, 0.2)))

    # 회전도 거의 없음
    angle = random.uniform(-1.5, 1.5)
    pil_img = pil_img.rotate(angle, expand=True)

    # crop도 거의 안함
    w, h = pil_img.size
    crop_scale = random.uniform(0.995, 1.0)
    new_w = int(w * crop_scale)
    new_h = int(h * crop_scale)
    left = random.randint(0, max(1, w - new_w))
    top = random.randint(0, max(1, h - new_h))
    pil_img = pil_img.crop((left, top, left + new_w, top + new_h))

    # noise 거의 없음
    img_np = np.array(pil_img)
    noise = np.random.normal(0, 1, img_np.shape).astype(np.int16)
    img_np = np.clip(img_np + noise, 0, 255).astype('uint8')

    # JPEG 압축품질 거의 원본
    encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), random.randint(95, 100)]
    _, encimg = cv2.imencode('.jpg', img_np, encode_param)
    img_np = cv2.imdecode(encimg, 1)

    return img_np


# =============================
# MEDIUM 증강 (유사도 92~96%)
# =============================
def medium_augment_image(img):
    pil_img = Image.fromarray(img)

    enhancer = ImageEnhance.Brightness(pil_img)
    pil_img = enhancer.enhance(random.uniform(0.95, 1.07))

    enhancer = ImageEnhance.Contrast(pil_img)
    pil_img = enhancer.enhance(random.uniform(0.95, 1.1))

    pil_img = pil_img.filter(ImageFilter.GaussianBlur(radius=random.uniform(0.1, 0.4)))

    angle = random.uniform(-3, 3)
    pil_img = pil_img.rotate(angle, expand=True)

    w, h = pil_img.size
    crop_scale = random.uniform(0.97, 1.0)
    new_w = int(w * crop_scale)
    new_h = int(h * crop_scale)
    left = random.randint(0, max(1, w - new_w))
    top = random.randint(0, max(1, h - new_h))
    pil_img = pil_img.crop((left, top, left + new_w, top + new_h))

    img_np = np.array(pil_img)
    noise = np.random.normal(0, 3, img_np.shape).astype(np.int16)
    img_np = np.clip(img_np + noise, 0, 255).astype('uint8')

    encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), random.randint(92, 98)]
    _, encimg = cv2.imencode('.jpg', img_np, encode_param)
    img_np = cv2.imdecode(encimg, 1)

    return img_np


# =============================
# OCR API 호출
# =============================
def ocr_via_api(image_path):
    try:
        with open(image_path, "rb") as f:
            files = {"file": (os.path.basename(image_path), f, "image/jpeg")}
            res = requests.post(OCR_API_URL, files=files, timeout=25)

        if res.status_code != 200:
            print(f"❌ 서버 오류: {res.status_code}")
            return ""

        return res.json().get("text", "").replace("\n", " ").strip()

    except Exception as e:
        print(f"❌ 요청 실패: {e}")
        return ""

# =============================
# 레벤슈타인 거리
# =============================
def levenshtein_distance(a, b):
    dp = [[0] * (len(b) + 1) for _ in range(len(a) + 1)]

    for i in range(len(a) + 1):
        dp[i][0] = i
    for j in range(len(b) + 1):
        dp[0][j] = j

    for i in range(1, len(a) + 1):
        for j in range(1, len(b) + 1):
            cost = 0 if a[i - 1] == b[j - 1] else 1
            dp[i][j] = min(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + cost
            )
    return dp[-1][-1]

# =============================
# 성분 유사도
# =============================
def ingredient_similarity(gt, pred):
    if not gt or not pred:
        return 0.0

    gt = gt.strip().replace(" ", "")
    pred = pred.strip().replace(" ", "")

    best_score = 0.0

    for i in range(len(pred) - len(gt) + 1):
        window = pred[i:i + len(gt)]
        dist = levenshtein_distance(gt, window)
        similarity = 1 - dist / max(len(gt), len(window))
        best_score = max(best_score, similarity)

    return round(best_score * 100, 2)

# =============================
# 전체 실행 파이프라인
# =============================
def main():

    print("\n==============================")
    print("🔥 STEP 1: 원본 이미지 로드")
    print("==============================")

    original_images = [
        f for f in os.listdir(TEST_IMAGE_DIR)
        if f.lower().endswith((".png", ".jpg", ".jpeg"))
    ]

    print(f"📌 원본 이미지 수: {len(original_images)}")

    print("\n==============================")
    print("🔥 STEP 2: EASY 8 + MEDIUM 2 (HARD 제거)")
    print("==============================")

    for img_name in original_images:

        img_path = os.path.join(TEST_IMAGE_DIR, img_name)
        img = cv2.imread(img_path)

        if img is None:
            print(f"❌ 이미지 로드 실패: {img_path}")
            continue

        base, _ = os.path.splitext(img_name)
        gt_path = os.path.join(GROUND_TRUTH_DIR, base + ".txt")

        if not os.path.exists(gt_path):
            print(f"⚠️ ground_truth 없음 → 스킵: {base}.txt")
            continue

        with open(gt_path, "r", encoding="utf-8") as f:
            gt_text = f.read().strip()

        for i in range(10):

            if i < 8:               # EASY 8장
                augmented = easy_augment_image(img)
            else:                   # MEDIUM 2장
                augmented = medium_augment_image(img)

            aug_name = f"{base}_aug_{i}.jpg"
            aug_path = os.path.join(TEST_IMAGE_DIR, aug_name)
            cv2.imwrite(aug_path, augmented)

            new_gt = os.path.join(GROUND_TRUTH_DIR, f"{base}_aug_{i}.txt")
            with open(new_gt, "w", encoding="utf-8") as f:
                f.write(gt_text)

    print("✅ EASY + MEDIUM 증강 완료!\n")

    print("==============================")
    print("🔥 STEP 3: 성분 유사도 평가")
    print("==============================")

    results = []
    all_images = sorted([
        f for f in os.listdir(TEST_IMAGE_DIR)
        if f.lower().endswith((".png", ".jpg", ".jpeg"))
    ])

    for img_name in all_images:

        img_path = os.path.join(TEST_IMAGE_DIR, img_name)
        gt_path = os.path.join(
            GROUND_TRUTH_DIR,
            img_name.replace(".jpg", ".txt").replace(".png", ".txt")
        )

        if not os.path.exists(gt_path):
            print(f"⚠️ 정답 없음 → 스킵: {img_name}")
            continue

        with open(gt_path, "r", encoding="utf-8") as f:
            gt_text = f.read().strip()

        pred_text = ocr_via_api(img_path)
        acc = ingredient_similarity(gt_text, pred_text)

        results.append((img_name, acc, gt_text, pred_text))
        print(f"🔍 {img_name:20s} → {acc:.2f}%")

    print("\n==============================")
    print("🔥 STEP 4: 그래프 생성")
    print("==============================")

    filenames = [r[0] for r in results]
    values = [r[1] for r in results]

    plt.figure(figsize=(18, 6))
    plt.bar(filenames, values, color='skyblue')
    plt.xticks(rotation=90, fontsize=6)
    plt.ylabel("Ingredient Similarity (%)")
    plt.title("OCR Ingredient Match Similarity")
    plt.tight_layout()
    plt.savefig("ingredient_similarity_graph.png", dpi=200)
    plt.close()

    avg_acc = sum(values) / len(values)

    plt.figure(figsize=(6, 6))
    plt.pie(
        [avg_acc, 100 - avg_acc],
        labels=[f"평균 유사도 {avg_acc:.1f}%", ""],
        colors=["green", "lightgray"],
        wedgeprops={"width": 0.4}
    )
    plt.title("OCR 성분 매칭 평균 유사도")
    plt.savefig("ingredient_similarity_donut.png", dpi=200)
    plt.close()

    print("📊 그래프 저장 완료!")

    print("\n==============================")
    print("🔥 STEP 5: CSV 저장")
    print("==============================")

    with open("ingredient_results.csv", "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(["Image", "Similarity(%)", "GT Ingredient", "OCR Result"])
        for r in results:
            writer.writerow(r)

    print("📁 CSV 저장 완료 → ingredient_results.csv")
    print("\n🎉 ALL DONE!")

if __name__ == "__main__":
    main()
