#include <iostream> // TODO: Remove after testing is done
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>


void displayImage(const cv::Mat &image, const std::string &title) {
	cv::namedWindow(title, cv::WINDOW_AUTOSIZE );
	cv::imshow(title, image);
	cv::waitKey(0);
}


cv::Mat readImageFromFile(const std::string &imagePath, const int &flags) {
	cv::Mat image;
	image = cv::imread(imagePath, flags);
	if (!image.data) {
		std::cout << "Error: Image could not be read from " << imagePath << "." << std::endl;
		throw;
	}
	return image;
}


void outputImageToFile(const std::string &imagePath, const cv::Mat &image) {
	imwrite(imagePath, image);
}


void applyNonBlindWatermark(const std::string &inputImagePath, const std::string &outputImagePath, const std::string &watermarkImagePath) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Non-blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Watermark: " << watermarkImagePath << std::endl << std::endl;

	// Read input image and watermark images from the corresponding input files
	cv::Mat inputImage = readImageFromFile(inputImagePath, CV_LOAD_IMAGE_COLOR);
	cv::Mat watermarkImage = readImageFromFile(watermarkImagePath, CV_LOAD_IMAGE_COLOR);

	// Display the input and watermark images; TODO: remove after testing is done
	displayImage(inputImage, "Non-blind watermarking, input image");
	displayImage(watermarkImage, "Non-blind watermarking, watermark image");

	// Convert watermark image to grayscale
	// TODO

	// Blend watermark image on input image
	cv::Mat watermarkedImage;
	cv::addWeighted(inputImage, 1.0, watermarkImage, 0.35, 0.0, watermarkedImage);

	// Display the watermarked image; TODO: remove after testing is done
	displayImage(watermarkedImage, "Non-blind watermarking, result image");

	// Write resulting image into output file
	outputImageToFile(outputImagePath, watermarkedImage);
}


void applyBlindWatermark(const std::string &inputImagePath, const std::string &outputImagePath, const std::string &message, const std::string &alphabet, const unsigned int &bitsUsed) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Message:   " << message << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	cv::Mat inputImage = readImageFromFile(inputImagePath, CV_LOAD_IMAGE_COLOR);

	// Convert message into binary code based on the alphabet supplied
	// TODO

	// Apply the binary code to the number of least significant bits supplied
	// TODO

	// Write resulting image into output file
	// TODO
}


std::string readBlindWatermark(const std::string &imagePath, const std::string &alphabet, const unsigned int &bitsUsed) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Reading blind watermark:" << std::endl;
	std::cout << "  Image:     " << imagePath << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	cv::Mat image = readImageFromFile(imagePath, CV_LOAD_IMAGE_COLOR);

	// Read binary code from image based on the number of least significant bits used
	// TODO

	// Convert binary code to a message based on the alphabet supplied
	std::string message = "";
	// TODO

	// Return the message read from the image
	return message;
}


// TODO: Remove after testing is done
int main() {
	// Non-blind watermarking
	applyNonBlindWatermark("image.png", "nonblind.png", "watermark.png");

	// Blind watermarking
	applyBlindWatermark("image.png", "blind.png", "This is copyrighted material!", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2);

	// Read blind watermark
	std::string watermark = readBlindWatermark("blind.png", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2);
	std::cout << "Watermark:" << watermark << std::endl;

	return 0;
}
