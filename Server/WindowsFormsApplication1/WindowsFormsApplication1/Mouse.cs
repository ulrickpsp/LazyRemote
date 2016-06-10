using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowsFormsApplication1
{
    class Mouse
    {
        private const int MOUSEEVENTF_LEFTDOWN = 0x02;
        private const int MOUSEEVENTF_LEFTUP = 0x04;
        private const int MOUSEEVENTF_RIGHTDOWN = 0x08;
        private const int MOUSEEVENTF_RIGHTUP = 0x10;

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        public static extern void mouse_event(long dwFlags, long dx, long dy, long cButtons, long dwExtraInfo);
        [DllImport("user32")]
        private static extern int SetCursorPos(int x, int y);


        public static void SetCursorAtNewPosition(int Ax, int Ay)
        {

            var new_positionX = getMousePositionX() + Ax;
            var new_positionY = getMousePositionY() + Ay;

            SetCursorPos(new_positionX, new_positionY);
        }

        public static void PerformLeftClick() {

            mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, getMousePositionX(), getMousePositionY(), 0, 0);
        }

        public static void PerformRightClick()
        {

            mouse_event(MOUSEEVENTF_RIGHTDOWN | MOUSEEVENTF_RIGHTUP, getMousePositionX(), getMousePositionY(), 0, 0);
        }


        private static int getMousePositionX()
        {

            return Cursor.Position.X;
        }

        private static int getMousePositionY()
        {
            return Cursor.Position.Y;
        }

    }

}
